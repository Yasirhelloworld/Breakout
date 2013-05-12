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
	
/** Pause time between updating animation frames */
	private static final double PAUSE_TIME = 10; // in ms
	
/** Initialise paddle object */
	private GRect PADDLE;
	
/** paddle y origin */
	private static final int PADDLE_Y_ORIGIN = APPLICATION_HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT / 2;
	
/** Initialise ball object */
	private GOval BALL;
	private double vx, vy; //velocity components of ball
	private static final double BALL_VY_INITIAL = 3.0; //initial y velocity of ball
	
/** Ball initial position */
	private static final int BALL_X_INITIAL = APPLICATION_WIDTH / 2;
	private static final int BALL_Y_INITIAL = APPLICATION_HEIGHT / 2;
	
/** Ball initial number of lives */
	private static final int NLIVES_INITIAL = 3;
	
/** Initialise remaining lives tracker and label */
	private GLabel livesRemainingLabel;
	private int livesRemaining;
	
/** Message box dimensions and font style */
	private static final int MESSAGE_WIDTH = 200;
	private static final int MESSAGE_HEIGHT = 100;
	private static final Font MESSAGE_FONT = new Font("Sans", Font.PLAIN, 32);
	private static final int MESSAGE_FUDGE_FACTOR = -5; // a value in pixels that adjusts the y position of the displayed message (to make it look more aesthetically pleasing)
	
/** Instantiate random number generator */
	private RandomGenerator rgen = RandomGenerator.getInstance();
	
/** Load audioclip object */
	private AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");

/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		setUpGame();
		runGame();
	}
	
	/** sets up the game board with colored bricks */
	private void setUpGame() {
		// draw bricks
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
		// set up mouse listening
		addMouseListeners();
		// initialise and add paddle
		initPaddle(APPLICATION_WIDTH / 2);
		// initialise and add lives remaining display
		livesRemaining = NLIVES_INITIAL;
		updateLivesRemaining();
	}
	
	/** 
	 * Runs the main game program
	 */
	private void runGame() {
		// initialise ball object
		initBall(BALL_X_INITIAL, BALL_Y_INITIAL);
		// kick the ball off with some initial velocity
		vy = BALL_VY_INITIAL;
		vx = rgen.nextDouble(1.0, 3.0);
		if (rgen.nextBoolean(0.5)) vx = -vx;
		int nBricks = NBRICKS_PER_ROW * NBRICK_ROWS;
		int nLives = NLIVES_INITIAL;		
		
		/* Main animation loop */
		while (true) {
			BALL.move(vx, vy);
			pause(PAUSE_TIME);
			// bounce if we hit a wall
			if (ballHitVerticalWall()) {
				vx = -vx;
			}
			if (ballHitTopWall()) {
				vy = -vy;
			} else if (ballHitBottomWall()) {
				livesRemaining -= 1;
				updateLivesRemaining();
				showMessage("GAME OVER");
				break;
			}
			// bounce upwards if we hit an object below
			GObject collider = getCollidingObjectBottom();
			if (collider == PADDLE) {
				vy = -Math.abs(vy);
				/* if ball hits edge of paddle from which ball is coming, also bounce x */
				if (((BALL.getX() + BALL_RADIUS) - (PADDLE.getX() + PADDLE_WIDTH / 2)) > PADDLE_WIDTH / 4) { //right hand side of paddle
					vx = Math.abs(vx);
				} else if (((BALL.getX() + BALL_RADIUS) - (PADDLE.getX() + PADDLE_WIDTH / 2)) < -PADDLE_WIDTH / 4) { //left hand side of paddle
					vx = -Math.abs(vx);
				}
				bounceClip.play();
			} else if (collider != null) { // we hit a brick
				vy = -Math.abs(vy);
				remove(collider);
				nBricks -= 1;
				bounceClip.play();
			}
			// bounce downwards if we hit an object above
			collider = getCollidingObjectTop();
			if (collider == PADDLE) {
				vy = Math.abs(vy);
				bounceClip.play();
			} else if (collider != null) { // we hit a brick
				vy = Math.abs(vy);
				remove(collider);
				nBricks -= 1;
				bounceClip.play();
			}
			if (nBricks == 0) {
				showMessage("YOU WIN!");
				break;
			}
		}
	}
	
	/**
	 * Iterate over the bottom two corners of the ball hit box and see if 
	 * we hit an object
	 * 
	 * @return GObject that we collided with. If none, return null
	 */
	private GObject getCollidingObjectBottom() {
		// grab elements
		GObject bottomLeftElement = getElementAt(BALL.getX(), (BALL.getY() + BALL_RADIUS * 2));
		GObject bottomRightElement = getElementAt((BALL.getX() + BALL_RADIUS * 2), (BALL.getY() + BALL_RADIUS * 2));
		// return element we collided with
		if (bottomRightElement != null) {
			return bottomRightElement;
		} else if (bottomLeftElement != null) {
			return bottomLeftElement;
		} else {
			return null;
		}
	}
	
	/**
	 * Iterate over the top two corners of the ball hit box and see if 
	 * we hit an object
	 * 
	 * @return
	 */
	private GObject getCollidingObjectTop() {
		// grab elements
		GObject topLeftElement = getElementAt(BALL.getX(), BALL.getY());
		GObject topRightElement = getElementAt((BALL.getX() + BALL_RADIUS * 2), BALL.getY());
		// return element we collided with
		if (topLeftElement != null) {
			return topLeftElement;
		} else if (topRightElement != null) {
			return topRightElement;
		} else {
			return null;
		}
	}
	
	/**
	 * Did we hit the top wall?
	 * 
	 * @param true or false
	 */
	private boolean ballHitTopWall() {
		if (BALL.getY() <= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Did we hit the bottom?
	 * 
	 * @param true or false
	 */
	private boolean ballHitBottomWall() {
		if (BALL.getY() >= (APPLICATION_HEIGHT - (BALL_RADIUS * 2))) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Did we hit a side wall?
	 * 
	 * @param true or false
	 */
	private boolean ballHitVerticalWall() {
		if (BALL.getX() <= 0 || BALL.getX() >= (APPLICATION_WIDTH - (BALL_RADIUS * 2))) {
			return true;
		} else {
			return false;
		}
	}
	
	/** 
	 * Initialises ball object at:
	 * 
	 * @param x
	 * @param y
	 * 
	 * (coordinates describe center of ball)
	 * 
	 */
	private void initBall(int x, int y) {
		int x0 = x - BALL_RADIUS;
		int y0 = y - BALL_RADIUS;
		this.BALL = new GOval(BALL_RADIUS * 2, BALL_RADIUS * 2);
		BALL.setLocation(x0, y0);
		BALL.setFilled(true);
		BALL.setFillColor(Color.black);
		add(this.BALL);
	}
	
	/**
	 * Listens for mouse movements and move paddle appropriately
	 * Paddle movement is restricted by bounds of application window
	 */
	public void mouseMoved(MouseEvent e) {
		int x;
		/* set bounds on paddle movement so it's edges cannot leave the screen */
		if (e.getX() < PADDLE_WIDTH / 2) {
			x = PADDLE_WIDTH / 2;
		} else if (e.getX() > (APPLICATION_WIDTH - PADDLE_WIDTH / 2)) {
			x = APPLICATION_WIDTH - PADDLE_WIDTH / 2;
		} else {
			x = e.getX();
		}
		this.PADDLE.setLocation((x - PADDLE_WIDTH / 2), PADDLE_Y_ORIGIN);
	}
		
	/** 
	 * Initialises paddle object at coordinates x, y
	 * 
	 * @param x coordinate of paddle 
	 * @param y coordinate of paddle
	 */
	private void initPaddle(int x) {
		int x0 = x - PADDLE_WIDTH / 2;
		this.PADDLE = new GRect(PADDLE_WIDTH, PADDLE_HEIGHT);
		PADDLE.setLocation(x0, PADDLE_Y_ORIGIN);
		PADDLE.setFilled(true);
		PADDLE.setFillColor(Color.black);
		add(this.PADDLE);
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
	//		brick.setColor(color); // uncomment to remove borders
		add(brick);
		}
	}
	
	/**
	 * prints a label centered to the screen indicating to the the user that the game is over (or won)
	 */
	private void showMessage(String message) {
		GRect messageBox = new GRect((APPLICATION_WIDTH - MESSAGE_WIDTH) / 2, (APPLICATION_HEIGHT - MESSAGE_HEIGHT) / 2, MESSAGE_WIDTH, MESSAGE_HEIGHT);
		messageBox.setFillColor(Color.LIGHT_GRAY);
		messageBox.setFilled(true);
		add(messageBox);
		GLabel messageText = new GLabel(message);
		messageText.setFont(MESSAGE_FONT);
		double xLabel = (APPLICATION_WIDTH - messageText.getWidth()) / 2;
		double yLabel = (APPLICATION_HEIGHT + messageText.getHeight()) / 2 + MESSAGE_FUDGE_FACTOR; // the fudge factor is to make it look more centered
		messageText.setLocation(xLabel, yLabel);
		add(messageText);
	}
	
	/**
	 * draws/updates livesRemaining label
	 */
	private void updateLivesRemaining() {
		String labelText = "Lives remaining = " + livesRemaining;
		livesRemainingLabel = new GLabel(labelText);
		double width = livesRemainingLabel.getWidth();
		double height = livesRemainingLabel.getHeight();
		livesRemainingLabel.setLocation(APPLICATION_WIDTH - width, height);
		add(livesRemainingLabel);
		
	}
}