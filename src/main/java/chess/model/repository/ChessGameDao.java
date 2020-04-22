package chess.model.repository;

import static chess.model.repository.template.JdbcTemplate.getPssFromParams;
import static chess.model.repository.template.JdbcTemplate.makeQuery;

import chess.model.domain.board.TeamScore;
import chess.model.domain.piece.Color;
import chess.model.repository.template.JdbcTemplate;
import chess.model.repository.template.PreparedStatementSetter;
import chess.model.repository.template.ResultSetMapper;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ChessGameDao {

    private final static ChessGameDao INSTANCE = new ChessGameDao();

    private ChessGameDao() {
    }

    public static ChessGameDao getInstance() {
        return INSTANCE;
    }

    public int insert(int roomId, Color gameTurn, Map<Color, String> userNames,
        TeamScore teamScore) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "INSERT INTO CHESS_GAME_TB(ROOM_ID, TURN_NM, BLACK_USER_NM, WHITE_USER_NM, BLACK_SCORE, WHITE_SCORE)",
            "VALUES (?, ?, ?, ?, ?, ?)"
        );
        PreparedStatementSetter pss = getPssFromParams(roomId, gameTurn.getName()
            , userNames.get(Color.BLACK), userNames.get(Color.WHITE)
            , teamScore.get(Color.BLACK), teamScore.get(Color.WHITE));
        return jdbcTemplate.executeUpdateWithGeneratedKey(query, pss);
    }

    public void updateProceedN(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "UPDATE CHESS_GAME_TB",
            "   SET PROCEEDING_YN = 'N'",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void updateTurn(int gameId, Color gameTurn) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "UPDATE CHESS_GAME_TB",
            "   SET TURN_NM = ?",
            " WHERE ID = ?",
            "   AND PROCEEDING_YN = 'Y'"
        );
        PreparedStatementSetter pss = getPssFromParams(gameTurn.getName(), gameId);
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void updateScore(int gameId, TeamScore teamScore) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "UPDATE CHESS_GAME_TB",
            "   SET BLACK_SCORE = ?",
            "     , WHITE_SCORE = ?",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = getPssFromParams(teamScore.get(Color.BLACK),
            teamScore.get(Color.WHITE), gameId);
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void delete(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "DELETE FROM CHESS_GAME_TB",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        jdbcTemplate.executeUpdate(query, pss);
    }

    public Optional<Integer> getGameNumberLatest(int roomId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT GAME.ID",
            "  FROM CHESS_GAME_TB AS GAME",
            "  JOIN ROOM_TB AS ROOM",
            " WHERE GAME.ROOM_ID = ROOM.ID",
            "   AND GAME.PROCEEDING_YN = 'Y'",
            "   AND ROOM.ID = ?",
            " ORDER BY ID DESC",
            " LIMIT 1"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, roomId);
        ResultSetMapper<Optional<Integer>> mapper = rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(rs.getInt("ID"));
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public Optional<Color> getGameTurn(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT TURN_NM",
            "  FROM CHESS_GAME_TB",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Optional<Color>> mapper = rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.ofNullable(Color.of(rs.getString("TURN_NM")));
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public Map<Color, String> getUserNames(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT BLACK_USER_NM",
            "     , WHITE_USER_NM",
            "  FROM CHESS_GAME_TB",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Map<Color, String>> mapper = rs -> {
            Map<Color, String> userNames = new HashMap<>();
            if (!rs.next()) {
                return userNames;
            }
            userNames.put(Color.BLACK, rs.getString("BLACK_USER_NM"));
            userNames.put(Color.WHITE, rs.getString("WHITE_USER_NM"));
            return Collections.unmodifiableMap(userNames);
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public Optional<Boolean> isProceeding(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT PROCEEDING_YN",
            "  FROM CHESS_GAME_TB",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Optional<Boolean>> mapper = rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(rs.getString("PROCEEDING_YN").equalsIgnoreCase("Y"));
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public Optional<Integer> getRoomId(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT ROOM_ID",
            "  FROM CHESS_GAME_TB",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Optional<Integer>> mapper = rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(rs.getInt("ROOM_ID"));
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public Map<Color, Double> getScores(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT BLACK_SCORE",
            "     , WHITE_SCORE",
            "  FROM CHESS_GAME_TB",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Map<Color, Double>> mapper = rs -> {
            Map<Color, Double> selectTeamScore = new HashMap<>();
            if (!rs.next()) {
                return selectTeamScore;
            }
            selectTeamScore.put(Color.BLACK, rs.getDouble("BLACK_SCORE"));
            selectTeamScore.put(Color.WHITE, rs.getDouble("WHITE_SCORE"));
            return selectTeamScore;
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public List<Integer> getProceedGameIdsByRoomId(int roomId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT ID",
            "  FROM CHESS_GAME_TB",
            " WHERE ROOM_ID = ?",
            "   AND PROCEEDING_YN = 'Y'"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, roomId);
        ResultSetMapper<List<Integer>> mapper = rs -> {
            List<Integer> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("ID"));
            }
            return ids;
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public boolean isProceed(int gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT ID",
            "  FROM CHESS_GAME_TB",
            " WHERE ID = ?",
            "   AND PROCEEDING_YN = 'Y'"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Boolean> mapper = ResultSet::next;
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }
}