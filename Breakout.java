/*
 * File: Breakout.java
 * -------------------
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board
 *  Should not be used directly (use getWidth()/getHeight() instead).
 *  * */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;
	
/** Offset of the leftmost brick column from the left */
	private static final int BRICK_X_OFFSET = BRICK_SEP / 2; // fudge factor, adjust to something that looks good

/** Number of turns */
	private static final int NTURNS = 3;

/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		setUpGame();
		runGame();
	}
	
	/** sets up the game board with colored bricks */
	private void setUpGame() {
		for (int i = 0; i < NBRICK_ROWS; i++) {
			Color brickColor;
			if (i < 2) {
				brickColor = Color.red;
			} else if (i < 4) {
				brickColor = Color.orange;
			} else if (i < 6) {
				brickColor = Color.yellow;
			} else if (i < 8) {
				brickColor = Color.green;
			} else {
				brickColor = Color.cyan;
			}
			drawRow(BRICK_X_OFFSET, (BRICK_Y_OFFSET + i * (BRICK_SEP + BRICK_HEIGHT)), brickColor);
		}
	}
	
	/** 
	 * Runs the main game program
	 */
	private void runGame(){
		// game code goes here
		drawPaddle(100);
		}
	
	/** 
	 * Draws a row of bricks
	 * 
	 * @param x origin of row (left)
	 * @param y origin of row (top)
	 * @param color of row
	 */
	private void drawRow(int x, int y, Color color) {
		for (int i = 0; i < NBRICKS_PER_ROW; i++) {
			GRect brick = new GRect((x + i * (BRICK_WIDTH + BRICK_SEP)), y, BRICK_WIDTH, BRICK_HEIGHT);
			brick.setFilled(true);
			brick.setFillColor(color);
//			brick.setColor(color); // uncomment to remove borders
			add(brick);
		}
	}
	/** Draw paddle centered at coordinate x (y is fixed)
	 * 
	 * @param x coordinate of paddle 
	 */
	private void drawPaddle(double x) {
		double x0 = x - PADDLE_WIDTH / 2;
		double y0 = APPLICATION_HEIGHT - PADDLE_Y_OFFSET - (PADDLE_HEIGHT / 2);
		GRect paddle = new GRect(x0, y0, PADDLE_WIDTH, PADDLE_HEIGHT);
		add(paddle);
	}
}
