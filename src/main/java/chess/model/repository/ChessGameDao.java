package chess.model.repository;

import static chess.model.repository.template.JdbcTemplate.getPssFromParams;
import static chess.model.repository.template.JdbcTemplate.makeQuery;

import chess.model.domain.board.TeamScore;
import chess.model.domain.piece.Team;
import chess.model.dto.GameInfoDto;
import chess.model.repository.template.JdbcTemplate;
import chess.model.repository.template.PreparedStatementSetter;
import chess.model.repository.template.ResultSetMapper;
import java.util.ArrayList;
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

    public Integer create(Integer roomId, Team gameTurn, Map<Team, String> userNames,
        TeamScore teamScore) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "INSERT INTO CHESS_GAME_TB(ROOM_ID, TURN_NM, BLACK_USER_NM, WHITE_USER_NM, BLACK_SCORE, WHITE_SCORE)",
            "VALUES (?, ?, ?, ?, ?, ?)"
        );
        PreparedStatementSetter pss = getPssFromParams(roomId, gameTurn.getName()
            , userNames.get(Team.BLACK), userNames.get(Team.WHITE)
            , teamScore.get(Team.BLACK), teamScore.get(Team.WHITE));
        return jdbcTemplate.executeUpdateWithGeneratedKey(query, pss);
    }

    public Optional<Integer> findProceedGameIdLatest(Integer roomId) {
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

    public Optional<Team> findCurrentTurn(Integer gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT TURN_NM",
            "  FROM CHESS_GAME_TB",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Optional<Team>> mapper = rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.ofNullable(Team.of(rs.getString("TURN_NM")));
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public Optional<Integer> findRoomId(Integer gameId) {
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

    public List<Integer> findProceedGameIdsBy(Integer roomId) {
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

    public Optional<GameInfoDto> findInfo(Integer gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "SELECT TURN_NM",
            "     , BLACK_USER_NM",
            "     , WHITE_USER_NM",
            "     , BLACK_SCORE",
            "     , WHITE_SCORE",
            "  FROM CHESS_GAME_TB",
            " WHERE ID = ?",
            "   AND PROCEEDING_YN = 'Y'"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        ResultSetMapper<Optional<GameInfoDto>> mapper = rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            Map<Team, String> userNames = new HashMap<>();
            userNames.put(Team.BLACK, rs.getString("BLACK_USER_NM"));
            userNames.put(Team.WHITE, rs.getString("WHITE_USER_NM"));
            Map<Team, Double> teamScores = new HashMap<>();
            teamScores.put(Team.BLACK, rs.getDouble("BLACK_SCORE"));
            teamScores.put(Team.WHITE, rs.getDouble("WHITE_SCORE"));
            return Optional.of(new GameInfoDto(Team.of(rs.getString("TURN_NM")), userNames,
                new TeamScore(teamScores)));
        };
        return jdbcTemplate.executeQuery(query, pss, mapper);
    }

    public void update(Integer gameId, Team gameTurn, TeamScore teamScore, boolean proceed) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "UPDATE CHESS_GAME_TB",
            "   SET TURN_NM = ?",
            "     , BLACK_SCORE = ?",
            "     , WHITE_SCORE = ?",
            "     , PROCEEDING_YN = ?",
            " WHERE ID = ?",
            "   AND PROCEEDING_YN = 'Y'"
        );
        PreparedStatementSetter pss = getPssFromParams(gameTurn.getName(),
            teamScore.get(Team.BLACK),
            teamScore.get(Team.WHITE), JdbcTemplate.convertYN(proceed), gameId);
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void updateProceedN(Integer gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "UPDATE CHESS_GAME_TB",
            "   SET PROCEEDING_YN = 'N'",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        jdbcTemplate.executeUpdate(query, pss);
    }

    public void delete(Integer gameId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        String query = makeQuery(
            "DELETE FROM CHESS_GAME_TB",
            " WHERE ID = ?"
        );
        PreparedStatementSetter pss = pstmt -> pstmt.setInt(1, gameId);
        jdbcTemplate.executeUpdate(query, pss);
    }
}
