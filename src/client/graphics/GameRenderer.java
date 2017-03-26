package client.graphics;
import client.GameState;
import client.blocks.Block;
import org.lwjgl.opengl.GL11;

import static client.ClientSettings.BLOCK_SIZE;
import static client.ClientSettings.SCREEN_HEIGHT;
import static client.ClientSettings.SCREEN_WIDTH;

class GameRenderer {

    private Draw draw;
    private GameState game;

    /**
     * Sets up a new game renderer to show the game on screen
     * @param textRenderers how to display text
     */
    GameRenderer(GameState game, TextRenderer[] textRenderers) {
        draw = new Draw(textRenderers);
        this.game = game;
    }

    /**
     * The main render method
     */
    void render() {
        int phase = 1;
        draw.colourBackground(phase);
        drawMap();
    }

    private void drawMap() {
        Block[][] b = game.getBlocks();
        for (int i = 0; i < SCREEN_WIDTH/BLOCK_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT/BLOCK_SIZE; j++) {
                if (b[i][j] != null) {
                    draw.drawBlock(i, j, b[i][j]);
                }
            }
        }
    }

    /**
     * Draws the the stencil for the pulse, including the layer underneath
     */
    private void drawStencil(int newPhase) {
        int oldPhase = 1;
        if (newPhase == 1) oldPhase = 0;

        //draws the old phase
        draw.colourBackground(oldPhase);
        drawMap();

        GL11.glEnable(GL11.GL_STENCIL_TEST);

        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF); // Set any stencil to 1
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glStencilMask(0xFF); // Write to stencil buffer
        GL11.glDepthMask(false); // Don't write to depth buffer
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // Clear stencil buffer (0 by default)

        //sets up the layer to be over drawn
    /*    draw.drawCircle(pulse
                .getStart().getX(), ClientSettings.SCREEN_HEIGHT - pulse
                .getStart().getY(), pulse
                .getRadius(), 500); */

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Pass test if stencil value is 1
        GL11.glStencilMask(0x00); // Don't write anything to stencil buffer
        GL11.glDepthMask(true); // Write to depth buffer
        GL11.glColorMask(true, true, true, true);

        //GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glColor3f(0, 0, 0);
    /*    draw.drawCircle(pulse
                .getStart().getX(), ClientSettings.SCREEN_HEIGHT - pulse
                .getStart().getY(), pulse
                .getRadius(), 500); */

        //draws the new phase in the circle
        draw.colourBackground(newPhase);
        drawMap();

        GL11.glDisable(GL11.GL_STENCIL_TEST);

        //pulse.draw();

    }
}