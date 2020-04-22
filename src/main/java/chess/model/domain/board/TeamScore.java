package chess.model.domain.board;

import chess.model.domain.piece.Color;
import chess.model.domain.piece.King;
import chess.model.domain.piece.Piece;
import chess.model.dto.GameResultDto;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import util.NullChecker;

public class TeamScore {

    private static final double PAWN_SAME_FILE_SCORE = -0.5;
    private static final double ZERO = 0.0;

    private final Map<Color, Double> teamScore;

    public TeamScore(Collection<Piece> pieces, Map<Color, Integer> pawnSameFileCountByColor) {
        NullChecker.validateNotNull(pieces, pawnSameFileCountByColor);
        this.teamScore = Collections
            .unmodifiableMap(getTeamScore(pieces, pawnSameFileCountByColor));
    }

    public TeamScore(Map<Color, Double> teamScore) {
        NullChecker.validateNotNull(teamScore);
        this.teamScore = Collections.unmodifiableMap(new HashMap<>(teamScore));
    }

    private Map<Color, Double> getTeamScore(Collection<Piece> pieces,
        Map<Color, Integer> pawnSameFileByColor) {
        Map<Color, Double> teamScore = new HashMap<>();
        for (Color color : Color.values()) {
            double piecesSumScore = getSumScore(pieces, color);
            double pawnChargeScore = pawnSameFileByColor.get(color) * PAWN_SAME_FILE_SCORE;
            teamScore.put(color, piecesSumScore + pawnChargeScore);
        }
        return teamScore;
    }

    private double getSumScore(Collection<Piece> pieces, Color color) {
        boolean noKing = pieces.stream()
            .filter(piece -> piece.isSameColor(color))
            .noneMatch(piece -> piece instanceof King);
        if (noKing) {
            return ZERO;
        }
        return pieces.stream()
            .filter(piece -> piece.isSameColor(color))
            .mapToDouble(Piece::getScore)
            .sum();
    }

    public List<Color> getWinners() {
        return teamScore.keySet().stream()
            .filter(color -> teamScore.get(color) == getWinningScore())
            .collect(Collectors.toList());
    }

    private double getWinningScore() {
        return teamScore.values().stream()
            .max(Double::compareTo)
            .orElseThrow(IllegalAccessError::new);
    }

    public Map<Color, Double> getTeamScore() {
        return teamScore;
    }

    public double get(Color color) {
        NullChecker.validateNotNull(color);
        return teamScore.get(color);
    }

    public GameResultDto getGameResult(Color color) {
        NullChecker.validateNotNull(color);
        return new GameResultDto(getWinCount(color), getDrawCount(), getLoseCount(color));
    }

    private int getWinCount(Color color) {
        if (getWinners().size() == 1
            && getWinners().contains(color)) {
            return 1;
        }
        return 0;
    }

    private int getLoseCount(Color color) {
        if (getWinners().size() == 1
            && !getWinners().contains(color)) {
            return 1;
        }
        return 0;
    }

    private int getDrawCount() {
        if (getWinners().size() == 2) {
            return 1;
        }
        return 0;
    }
}