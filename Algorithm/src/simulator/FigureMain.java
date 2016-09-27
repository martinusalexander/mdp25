package simulator;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class FigureMain extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private Button[][] button1 = new Button[20][15];
	private Button[][] button2 = new Button[20][15];
	private boolean isAdd = true;//

	/**
	 *
	 */
	public static void main(String[] args) {
		FigureMain frame = new FigureMain();
		frame.setVisible(true);
	}

	
	public FigureMain() {
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

		JButton btBegin = new JButton("import");
		btBegin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//select the fixed file
				getDatas("map.txt");
			}
		});
		panel.add(btBegin);

		JButton btSave = new JButton("export");
		btSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//
				saveDatas();
			}
		});
		panel.add(btSave);

		JButton btAdd = new JButton("add obstacle");
		btAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//
				isAdd = true;
			}
		});
		panel.add(btAdd);

		JButton btRemove = new JButton("remove obstacle");
		btRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//
				isAdd = false;
			}
		});
		panel.add(btRemove);

		JButton btExplore = new JButton("explore");
		panel.add(btExplore);

		JButton btFind = new JButton("find path");
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
		panel_2.setLayout(new GridLayout(20, 15, 0, 0));
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 15; j++) {
				button1[i][j] = new Button(" ");
				panel_2.add(button1[i][j]);
				button1[i][j].addActionListener(this);
				button1[i][j].setActionCommand(i + "a" + j);
			}
		}

		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3, BorderLayout.EAST);
		//exploration map
		panel_3.setLayout(new GridLayout(20, 15, 0, 0));
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 15; j++) {
				button2[i][j] = new Button(" ");
				panel_3.add(button2[i][j]);
				button2[i][j].addActionListener(this);
				button2[i][j].setActionCommand(i + "b" + j);
			}
		}

	}

	/**
	 *obstacle is black 
	 */
	public final static Color COLOR1 = Color.BLACK;
	/**
	 *
	 */
	public final static Color COLOR2 = UIManager.getColor("Button.background");

	/**
	 *action
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		Button button = (Button) e.getSource();
		if (isAdd) {
			button.setBackground(COLOR1);
		} else {
			button.setBackground(COLOR2);
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
			/**
			 * 
			 */
			BigInteger num = new BigInteger(nextLine, 16);
			String mapBin = num.toString(2);
			System.out.println(mapBin);
			
			if (mapBin.length() != 304) {
				return;
			}

			int row = 19;
			for (int i = 2; i < 302; i++) {
				String cc = mapBin.charAt(i) + "";
				if (i > 16 && (i - 2) % 15 == 0) {
					row--;
				}
				/**
				 * set color
				 */
				if ("1".equals(cc)) {
					button1[row][(i - 2) % 15].setBackground(COLOR1);
				} else {
					button1[row][(i - 2) % 15].setBackground(COLOR2);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * export map
	 */
	private void saveDatas() {
		StringBuilder sb = new StringBuilder();
		sb.append("11");
		for (int i = 19; i >= 0; i--) {
			for (int j = 0; j <15; j++) {
				Color color = button1[i][j].getBackground();
				if (color == COLOR1) {
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
					new FileWriter("map.txt"));
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
	private void reset() {
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 15; j++) {
				button1[i][j].setBackground(COLOR2);
			}
		}
	}
}
