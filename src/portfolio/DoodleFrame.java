package portfolio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JToggleButton;

public class DoodleFrame extends JFrame implements MouseMotionListener, MouseListener {

	private static JPanel contentPane;
	private static JPanel panel;
	private static JToggleButton drawDashedBtn;
	private Point lastPoint = null;
	private Scanner in;
	private PrintWriter out;
	private Socket socket;
	private Thread serverListener;
	private static final Color[] colors = { Color.BLACK, Color.RED, Color.BLUE, new Color(0x009F00),
			new Color(0xffa500), Color.MAGENTA };
	private static final double DASH_LEN = 31;
	private static final double SPACE_LEN = 3;
	private boolean drawFlag = true;
	private double dist = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DoodleFrame frame = new DoodleFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Point p1 = new Point(5, 2);
				Point p2 = new Point(11, 27);
//				System.out.println(snipLine(p1, p2, 10));
//				System.out.println(calculateDist(p1, p2));
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public DoodleFrame() {
		// Sets up socket and input/output
		try {
			socket = new Socket("localhost", 4444);
			in = new Scanner(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Creates thread for client to listen to server.
		serverListener = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					String clientPoints = in.nextLine();
					Scanner parser = new Scanner(clientPoints);
					drawLine(new Point(parser.nextInt(), parser.nextInt()),
							new Point(parser.nextInt(), parser.nextInt()), colors[parser.nextInt() % colors.length]);
					parser.close();
				}
			}
		});
		serverListener.start(); // Starts thread.

		// Frame settings
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		panel = new JPanel();
		contentPane.add(panel);
		drawDashedBtn = new JToggleButton("Dashed");
		contentPane.add(drawDashedBtn, BorderLayout.NORTH);
		panel.addMouseMotionListener(this);
		panel.addMouseListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent e) { // Sends mouse location to server
		Point currentPoint = e.getPoint();
		if (lastPoint != null) {// not beginning of a doodle
			if (!drawDashedBtn.isSelected()) {
				// draw continuous (not dashed)
				sendLine(lastPoint, currentPoint);
			} else {
				Point tempEnd = null;
				while (!currentPoint.equals(tempEnd)) {
					tempEnd = snipLine(lastPoint, currentPoint, (drawFlag ? DASH_LEN : SPACE_LEN) - dist);
//					System.out.printf("drawFlag: " + drawFlag + "  %.3f  " + lastPoint.toString() + "  "
//							+ currentPoint.toString() + "  " + tempEnd.toString()+"\n", dist);
					if (drawFlag)
						sendLine(lastPoint, tempEnd);
					dist += calculateDist(lastPoint, tempEnd);
					lastPoint = tempEnd;
					if (dist >= (drawFlag ? DASH_LEN : SPACE_LEN)) {
						dist = 0;
						drawFlag = !drawFlag;
					}
				}
			}
		}
		lastPoint = currentPoint;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Ends a doodle segment
		lastPoint = null;
		drawFlag = true;
		dist = 0;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Helper method to determine a new end point for a line which would make it
	 * a certain length
	 * 
	 * @param start
	 * @param end
	 * @param length
	 *            the desired line length
	 * @return a new end point for this line no further from start than end
	 */
	public static Point snipLine(Point start, Point end, double length) {
		double dist = calculateDist(start, end);
//		System.out.println(start + "  " + end + "  " + dist);
		if (length >= dist) {
			return end;
		} else {
			double fraction = Math.ceil(length) / dist;
			int newX = (int) Math.round((end.getX() - start.getX()) * fraction + start.getX());
			int newY = (int) Math.round((end.getY() - start.getY()) * fraction + start.getY());
			return new Point(newX, newY);
		}
	}

	/**
	 * Helper method to calculate the distance between two points
	 * 
	 * @param p1
	 * @param p2
	 * @return distance between the two given points
	 */
	private static double calculateDist(Point p1, Point p2) {
		double x = p1.distance(p2);
		return x;
	}

	/**
	 * Draws a line to our doodle frame between two given points in a given
	 * color
	 * 
	 * @param p1
	 * @param p2
	 * @param color
	 */
	private void drawLine(Point p1, Point p2, Color color) {
		Graphics2D g = (Graphics2D) panel.getGraphics();
		g.setColor(color);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	/**
	 * Sends coordinates of two points to the server which create a line to be
	 * drawn on other doodlefFrames
	 * 
	 * @param p1
	 * @param p2
	 */
	private void sendLine(Point p1, Point p2) {
		// System.out.printf("%d %d %d %d\n", p1.x, p1.y, p2.x, p2.y);
		out.printf("%d %d %d %d", p1.x, p1.y, p2.x, p2.y);
		out.println();
		out.flush();
	}
}
