package mdp;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class ArenaSimulator extends JFrame implements ActionListener {
	
	protected static Arena arena;
	protected static Robot robot;
	protected static ArenaSimulator simulator;

	private static final long serialVersionUID = 1L;
	
	public final static Color OBSTACLE_COLOR = Color.BLACK;
	public final static Color DEFAULT_COLOR = UIManager.getColor("Button.background");
	public final static Color VISITED_COLOR = Color.PINK;
	public final static Color ROBOT_POSITION_COLOR = Color.GREEN;
	public final static Color ROBOT_DIRECTION_COLOR = Color.BLUE;
	
	private JPanel contentPane;
	private Button[][] button1 = new Button[Arena.ARENA_HEIGHT][Arena.ARENA_LENGTH];
	private Button[][] button2 = new Button[Arena.ARENA_HEIGHT][Arena.ARENA_LENGTH];
	//private boolean isAdd = true;
	
	/*
	public static void main(String[] args) {
		arena = new Arena();
		simulator = new ArenaSimulator();
		simulator.setVisible(true);
		robot = new Robot(0, 0, Robot.HEADING_LEFT);
		
	}*/
	
	public ArenaSimulator() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 746, 401);
		//fixed size
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JButton btImport = new JButton("Import");
		JFileChooser  fileDialog = new JFileChooser();
		btImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//select the fixed file
				int returnVal = fileDialog.showOpenDialog(App.simulator);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileDialog.getSelectedFile();
					getDatas(file.getAbsolutePath());
	            }
			}
		});
		panel.add(btImport);

		JButton btExport = new JButton("Export");
		btExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileDialog.showOpenDialog(App.simulator);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileDialog.getSelectedFile();
		            saveDatas(file.getAbsolutePath());
	            }
			}
		});
		panel.add(btExport);

		/*JButton btAdd = new JButton("Add Obstacle");
		btAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isAdd = true;
			}
		});
		panel.add(btAdd);

		JButton btRemove = new JButton("Remove Obstacle");
		btRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isAdd = false;
			}
		});
		panel.add(btRemove);*/

		JButton btExplore = new JButton("Explore");
		btExplore.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				App.robot.command(Robot.EXPLORE);
				
			}
		});
		panel.add(btExplore);

		JButton btFind = new JButton("Fastest Run");
		btFind.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				App.robot.command(Robot.FASTEST_RUN);
				
			}
		});
		panel.add(btFind);

		JButton btReset = new JButton("Reset");
		btReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		panel.add(btReset);

		JPanel panel_4 = new JPanel();
		panel.add(panel_4);
		Dimension dimension = new Dimension(746, 50);
		panel_4.setPreferredSize(dimension);

		JLabel lblNewLabel = new JLabel("Maze");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblNewLabel);

		JPanel panel_5 = new JPanel();
		panel_4.add(panel_5);
		panel_5.setPreferredSize(new Dimension(346, 10));
		JLabel lblNewLabel_1 = new JLabel("Exploration");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblNewLabel_1);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.WEST);
		//maze map
		panel_2.setLayout(new GridLayout(Arena.ARENA_HEIGHT, Arena.ARENA_LENGTH, 0, 0));
		for (int i = 0; i < Arena.ARENA_HEIGHT; i++) {
			for (int j = 0; j < Arena.ARENA_LENGTH; j++) {
				button1[i][j] = new Button(" ");
				panel_2.add(button1[i][j]);
				button1[i][j].addActionListener(this);
				button1[i][j].setActionCommand("x" + j + "y" + (Arena.ARENA_HEIGHT-1-i));
			}
		}

		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3, BorderLayout.EAST);
		//exploration map
		panel_3.setLayout(new GridLayout(Arena.ARENA_HEIGHT, Arena.ARENA_LENGTH, 0, 0));
		for (int i = 0; i < Arena.ARENA_HEIGHT; i++) {
			for (int j = 0; j < Arena.ARENA_LENGTH; j++) {
				button2[i][j] = new Button(" ");
				panel_3.add(button2[i][j]);
				//button2[i][j].addActionListener(this);
				//button2[i][j].setActionCommand(i + "b" + j);
			}
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		int xCoordinate = Integer.parseInt(command.substring(1, command.indexOf("y")));
		int yCoordinate = Integer.parseInt(command.substring(command.indexOf("y") + 1));
		Button button = (Button) e.getSource();
		if (button.getBackground() == DEFAULT_COLOR) { 
			System.out.println(xCoordinate + " " + yCoordinate);
			App.arena.getGrid(xCoordinate, yCoordinate).markAsObstacle();
			button.setBackground(OBSTACLE_COLOR);
		} else {
			System.out.println(xCoordinate + " " + yCoordinate);
			App.arena.getGrid(xCoordinate, yCoordinate).markAsNotObstacle();
			button.setBackground(DEFAULT_COLOR);
		}
	}
	/**
	 * import maze
	 * 
	 * @param fileName
	 */
	private void getDatas(String fileName) {
		try {
			Scanner scanner = new Scanner(new File(fileName));
			String nextLine = scanner.nextLine();
			BigInteger num = new BigInteger(nextLine, 16);
			String mapBin = num.toString(2);
			//System.out.println(mapBin);
			
			if (mapBin.length() != 304) {
				return;
			}
			
			//mapBin = "1100000000000001100000000000000000000000000000000000000000000011111111110000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000011";
			//System.out.println("B" + mapBin.substring(2, 302).indexOf("1"));
			String data = mapBin.substring(2, mapBin.length()-2);
			//System.out.println(data.length());
			for (int i = 0; i < Arena.ARENA_HEIGHT; i++) {
				for (int j = 0; j < Arena.ARENA_LENGTH; j++) {
					char cc = data.charAt(i * Arena.ARENA_LENGTH + j);
					String c = String.valueOf(cc);
					if (c.equals("1")) {
						button1[Arena.ARENA_HEIGHT-1-i][j].setBackground(OBSTACLE_COLOR);
						App.arena.setGridAsObstacle(j, i);
					} else {
						button1[Arena.ARENA_HEIGHT-1-i][j].setBackground(DEFAULT_COLOR);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * export map
	 */
	private void saveDatas(String filePath) {
		StringBuilder sb = new StringBuilder();
		sb.append("11");
		for (int i = Arena.ARENA_HEIGHT-1; i >= 0; i--) {
			for (int j = 0; j < Arena.ARENA_LENGTH; j++) {
				Color color = button1[i][j].getBackground();
				if (color == OBSTACLE_COLOR) {
					sb.append("1");
				} else {
					sb.append("0");
				}
			}
		}
		sb.append("11");
		BigInteger num2 = new BigInteger(sb.toString(),2);
		String mapHex = num2.toString(16);
		
		try {
			/**
			 * 
			 */
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(filePath));
			writer.write(mapHex);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public void reset() {
		App.robot.reset();
		for (int i = 0; i < Arena.ARENA_HEIGHT; i++) {
			for (int j = 0; j < Arena.ARENA_LENGTH; j++) {
				button1[i][j].setBackground(DEFAULT_COLOR);
				button2[i][j].setBackground(DEFAULT_COLOR);
				App.arena.getGrid(j, i).reset();
			}
		}
		
	}
	
	protected void updateMap(String data1, String data2) {
		String mapData1;
		String mapData2;
		try {
			BigInteger num = new BigInteger(data1, 16);
			mapData1 = num.toString(2);
			mapData1 = mapData1.substring(2, 302);
			int data2Length = data2.length();
			data2Length *= 4;
			num = new BigInteger(data2, 16);
			mapData2 = num.toString(2);
			while (mapData2.length() < data2Length) {
				mapData2 = "0" + mapData2;
			}
		} catch (NumberFormatException e) {
			return;
		}
		
		int counter = 0;
		for (int j = 0; j < Arena.ARENA_HEIGHT; j++) {  // j => 0 ~ 19
			for (int i = 0; i < Arena.ARENA_LENGTH; i++) {  // i => 0 ~ 14
				char cc = mapData1.charAt(j * Arena.ARENA_LENGTH + i); //j * Arena.ARENA_LENGTH + i
				String c = String.valueOf(cc);
				if (c.equals("1")) {  // is visited
					//System.out.println(mapData2);
					button2[Arena.ARENA_HEIGHT-1-j][i].setBackground(VISITED_COLOR);
					try {
						char obstacleChar = mapData2.charAt(counter);  // obstacle(1)/space(0) bit
						counter++;
						String obstacleStr = String.valueOf(obstacleChar);
						if (obstacleStr.equals("1")) {
							button2[Arena.ARENA_HEIGHT-1-j][i].setBackground(OBSTACLE_COLOR);
						}
					} catch (StringIndexOutOfBoundsException e) {
						e.printStackTrace();
					}
				} else {  // is not visited
					button2[Arena.ARENA_HEIGHT-1-j][i].setBackground(DEFAULT_COLOR);
				}
			}
		}
		//Mark robot location
		button2[Arena.ARENA_HEIGHT-1-App.robot.getY()][App.robot.getX()].setBackground(ROBOT_POSITION_COLOR);
		switch (App.robot.getDirection()) {
			case Robot.HEADING_UP:
				button2[Arena.ARENA_HEIGHT-2-App.robot.getY()][App.robot.getX()].setBackground(ROBOT_DIRECTION_COLOR);
				break;
			case Robot.HEADING_DOWN:
				button2[Arena.ARENA_HEIGHT-App.robot.getY()][App.robot.getX()].setBackground(ROBOT_DIRECTION_COLOR);
				break;
			case Robot.HEADING_RIGHT:
				button2[Arena.ARENA_HEIGHT-1-App.robot.getY()][App.robot.getX()+1].setBackground(ROBOT_DIRECTION_COLOR);
				break;
			case Robot.HEADING_LEFT:
				button2[Arena.ARENA_HEIGHT-1-App.robot.getY()][App.robot.getX()-1].setBackground(ROBOT_DIRECTION_COLOR);
				break;
		}
	}
}
