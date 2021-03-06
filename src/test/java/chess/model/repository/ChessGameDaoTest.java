package chess.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import chess.model.domain.board.ChessGame;
import chess.model.domain.board.TeamScore;
import chess.model.domain.piece.Team;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChessGameDaoTest {

    private static final ChessGameDao CHESS_GAME_DAO = ChessGameDao.getInstance();
    private static final int ROOM_ID = 1;
    private static final Team GAME_TURN = Team.BLACK;
    private static final Map<Team, String> USER_NAMES;
    private static final String BLACK_NAME = "BLACK";
    private static final String WHITE_NAME = "WHITE";
    private static final TeamScore TEAM_SCORE = new ChessGame().deriveTeamScore();

    static {
        Map<Team, String> userNames = new HashMap<>();
        userNames.put(Team.BLACK, BLACK_NAME);
        userNames.put(Team.WHITE, WHITE_NAME);
        USER_NAMES = Collections.unmodifiableMap(new HashMap<>(userNames));
    }

    private int gameId;

    @BeforeEach
    void setup() {
        gameId = CHESS_GAME_DAO.create(ROOM_ID, GAME_TURN, USER_NAMES, TEAM_SCORE);
    }

    @AfterEach
    void tearDown() {
        CHESS_GAME_DAO.delete(gameId);
    }

    @Test
    void getInstance() {
        ChessGameDao chessGameDao1 = ChessGameDao.getInstance();
        ChessGameDao chessGameDao2 = ChessGameDao.getInstance();
        assertThat(chessGameDao1 == chessGameDao2).isTrue();
    }

    @Test
    void insert() {
        CHESS_GAME_DAO.delete(gameId);
        assertThat(CHESS_GAME_DAO.findCurrentTurn(gameId).isPresent()).isFalse();

        gameId = CHESS_GAME_DAO.create(ROOM_ID, GAME_TURN, USER_NAMES, TEAM_SCORE);
        assertThat(CHESS_GAME_DAO.findCurrentTurn(gameId).get()).isEqualTo(GAME_TURN);
   }

    @Test
    void updateProceedN() {
        assertThat(
            CHESS_GAME_DAO.findCurrentTurn(gameId).orElseThrow(IllegalArgumentException::new))
            .isEqualTo(GAME_TURN);

        CHESS_GAME_DAO.updateProceedN(gameId);
        assertThat(
            CHESS_GAME_DAO.findCurrentTurn(gameId).orElseThrow(IllegalArgumentException::new))
            .isEqualTo(GAME_TURN);
    }

    @Test
    void delete() {
        assertThat(CHESS_GAME_DAO.findCurrentTurn(gameId).get()).isEqualTo(GAME_TURN);

        CHESS_GAME_DAO.delete(gameId);
        assertThat(CHESS_GAME_DAO.findCurrentTurn(gameId).isPresent()).isFalse();
    }

    @Test
    void getGameNumberLatest() {
        assertThat(
            CHESS_GAME_DAO.findProceedGameIdLatest(ROOM_ID)
                .orElseThrow(IllegalArgumentException::new))
            .isGreaterThanOrEqualTo(0);
    }

    @Test
    void getGameTurn() {
        assertThat(
            CHESS_GAME_DAO.findCurrentTurn(gameId).orElseThrow(IllegalArgumentException::new))
            .isEqualTo(GAME_TURN);
    }

    @Test
    void getRoomId() {
        assertThat(CHESS_GAME_DAO.findRoomId(gameId).orElseThrow(IllegalArgumentException::new))
            .isEqualTo(ROOM_ID);
    }

}