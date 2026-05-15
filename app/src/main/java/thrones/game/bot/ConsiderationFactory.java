package thrones.game.bot;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory to create considerations by mapping abbreviations (OA, OD, OM, TA,
 * TD, TM) to consideration instances.
 */
public class ConsiderationFactory {

    public static Consideration fromCode(String code) {
        switch (code.trim().toUpperCase()) {
            case "OA":
                return new OpponentAttackConsideration();
            case "OD":
                return new OpponentDefenceConsideration();
            case "OM":
                return new OpponentMagicConsideration();
            case "TA":
                return new TeamAttackConsideration();
            case "TD":
                return new TeamDefenceConsideration();
            case "TM":
                return new TeamMagicConsideration();
            default:
                return null;
        }
    }

    public static List<Consideration> fromCodes(String csv) {
        List<Consideration> result = new ArrayList<>();
        if (csv == null || csv.isEmpty()) {
            return result;
        }
        for (String code : csv.split(",")) {
            Consideration c = fromCode(code);
            if (c != null) {
                result.add(c);
            }
        }
        return result;
    }
}
