package its.geppy.tictactoe.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.*;

public class BoardManager {

    final static double boardSize = 3;
    final static double particleStep = 0.25;
    final static double thickness = 0.02;

    public static void buildBoard(Player player, LivingEntity opponent) {
        Location center = player.getEyeLocation().add(opponent.getEyeLocation()).multiply(.5).subtract(0, boardSize/2, 0);

        ArmorStand stand = center.getWorld().spawn(center.clone().add(0, -1, 0), ArmorStand.class, (setting) -> {
            setting.setCustomName("tictactoe_stand");
            setting.setGravity(false);
            setting.setSmall(true);
            setting.setCollidable(false);
            setting.setInvisible(true);
        });

        Map<Vector, GameData.ParticleJob> particleMap = new HashMap<>();

        GameData game = new GameData(stand, particleMap, player, opponent);

        // Frame
        for (double x = -(boardSize/2); x < boardSize/2; x += particleStep) {
            for (double y = -(boardSize/2); y < boardSize/2; y += particleStep) {

                Vector vector = game.calculatePosition(x, y);

                if ((x + boardSize/2 > boardSize * (0.34 - thickness) && x + boardSize/2 < boardSize * (0.33 + thickness)) ||
                    (x + boardSize/2 > boardSize * (0.66 - thickness) && x + boardSize/2 < boardSize * (0.66 + thickness)) ||
                    (y + boardSize/2 > boardSize * (0.34 - thickness) && y + boardSize/2 < boardSize * (0.33 + thickness)) ||
                    (y + boardSize/2 > boardSize * (0.66 - thickness) && y + boardSize/2 < boardSize * (0.66 + thickness))) {

                    particleMap.put(vector, GameData.ParticleJob.FRAME);
                }

            }
        }

        // Clickables
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {

                double dx = boardSize * x / 3;
                double dy = boardSize * y / 3;

                Vector vector = game.calculatePosition(dx, dy);
                vector.subtract(new Vector(0, .3, 0));

                game.clickables.put(vector, new Byte[]{(byte) x, (byte) y});
                //particleMap.put(vector, GameData.ParticleJob.DEBUG);

            }
        }

        game.startGame();

    }

    public static void clicked(Vector clickable, GameData game) {
        GameData.Turn turn = game.nextTurn();
        List<String> shape;

        if (turn.equals(GameData.Turn.O))
            shape = xShape();
        else
            shape = oShape();

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {

                String current = shape.get(x + y * 5);
                if (current.equals(""))
                    continue;

                double dx = (x - 2) * (boardSize * 0.04);
                double dy = (y - 2) * (boardSize * 0.04);

                Vector vector = game.calculatePosition(clickable.toLocation(game.getChallenger().getWorld()).add(0, -(boardSize * 0.45), 0).toVector(), dx, dy);
                game.particleMap.put(vector, game.getTurn() == GameData.Turn.O ? GameData.ParticleJob.O : GameData.ParticleJob.X);

            }
        }

        game.removeClickable(clickable);

    }

    private static List<String> xShape() {
        return Arrays.asList(
                "x", "", "", "", "x",
                "", "x", "", "x", "",
                "", "", "x", "", "",
                "", "x", "", "x", "",
                "x", "", "", "", "x"
        );
    }

    private static List<String> oShape() {
        return Arrays.asList(
                "", "x", "x", "x", "",
                "x", "", "", "", "x",
                "x", "", "", "", "x",
                "x", "", "", "", "x",
                "", "x", "x", "x", ""
        );
    }

    public static Vector getClickable(Player player, GameData game) {

        Vector finalClickable = null;
        double smallestAngle = Double.MAX_VALUE;

        for (Vector clickable : game.getClickables().keySet()) {

            Vector direction = clickable.clone()
                    .subtract(player.getEyeLocation().toVector())
                    .normalize();
            double angle = direction.angle(player.getEyeLocation().getDirection());

            if (angle < smallestAngle) {
                smallestAngle = angle;
                finalClickable = clickable;
            }


        }

        if (smallestAngle > 0.25)
            finalClickable = null;

        return finalClickable;
    }
}
