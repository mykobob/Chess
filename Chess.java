import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Chess {

    private JFrame frame;

    private final JPanel gui = new JPanel(new BorderLayout(3, 3));
    private ChessSpot[][] chessboardSquares = new ChessSpot[8][8];
    private JPanel chessboard;

    private boolean[][] occupied = new boolean[8][8];
    private boolean[][] white = new boolean[8][8];
    private String[][] piece = new String[8][8];

    private Map<String, Image> images;
    private final String[] promotionPieces = {"Rook", "Knight", "Bishop", "Queen"};

    private JButton newButton;
    private JButton resignButton;
    private JLabel msg = new JLabel("Ready");
    private JLabel CHECK = new JLabel("");

    private int movesPlayed; // odd number means black turn
    private boolean onSelection = false;
    private List<Point> possibleMoves = new ArrayList<>();

    private boolean GAME_FINISHED = false;

    public Chess() {
        frame = new JFrame("Chess");
        images = new HashMap<>();
        initButtons();
        initGUI();
        initResources();
        frame.add(gui);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setVisible(true);
    }

    private void initButtons() {
        newButton = new JButton("New");
        newButton.addActionListener(e -> {
            initGame();
            msg.setText("White Turn");
//            System.out.println(images.keySet());
        });

        resignButton = new JButton("Resign");
        resignButton.addActionListener(e -> endTheGame(movesPlayed % 2 == 1));
    }

    private void initResources(){
        try {
            Scanner in = new Scanner(new File("gameLayout.in"));
            while(in.hasNext()) {
                String name = in.next();
                images.put(name, ImageIO.read(new File("Images/" + name + ".png")).getScaledInstance(64, 64, 1));
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    private void initGame() {
        try {
            Scanner in = new Scanner(new File("gameLayout.in"));
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    piece[i][j] = "";
                }
            }
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    chessboardSquares[i][j].setIcon(null);
                    occupied[i][j] = false;
                    white[i][j] = false;
                    piece[i][j] = "";
                }
            }
            int[] rows = {0, 6};
            for (int i = 0; i < 2; i++) {
                int row = rows[i];
                for (int j = 0; j < 2; j++) {
                    String[] line = in.nextLine().split(" ");
                    for (int kk = 0; kk < line.length; kk++) {
                        String name = line[kk];
                        chessboardSquares[row + j][kk].setIcon(new ImageIcon(images.get(name)));
                        occupied[row + j][kk] = true;
                        white[row + j][kk] = i == 1;
                        piece[row + j][kk] = name.substring(5);
                    }
                }
            }

            movesPlayed = 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doWhenClicked(ChessSpot b){
//        System.out.println("doWhenClicked");
        int r = b.row;
        int c = b.col;
        if (!onSelection) {
            if (movesPlayed % 2 == 0 && white[r][c] || movesPlayed % 2 == 1 && !white[r][c]) // even number means white turn
                {
                    switch (piece[r][c].toLowerCase()) {
                        case "pawn":
//                            System.out.println("pawn");
                            possibleMoves = GameLogic.getPawnMoves(r, c, occupied, white);
                            break;
                        case "rook":
//                            System.out.println("rook");
                            possibleMoves = GameLogic.getRookMoves(r, c, occupied, white, 15125, false);
                            break;
                        case "bishop":
//                            System.out.println("bishop");
                            possibleMoves = GameLogic.getBishopMoves(r, c, occupied, white, 135968, false);
                            break;
                        case "queen":
//                            System.out.println("queen");
                            possibleMoves = GameLogic.getQueenMoves(r, c, occupied, white, 910236, false);
                            break;
                        case "king":
//                            System.out.println("king");
                            possibleMoves = GameLogic.getKingMoves(r, c, occupied, white, piece);
                            break;
                        case "knight":
//                            System.out.println("knight");
                            possibleMoves = GameLogic.getKnightMoves(r, c, occupied, white, false);
                    }
                    if (possibleMoves.size() > 0) {
                        possibleMoves.add(0, new Point(r, c));
                        highlightBoxes(possibleMoves);
                        if(underCheck(white[r][c], white, possibleMoves))
                            CHECK.setText("   CHECK !!!");
                        else
                            CHECK.setText("");
                        onSelection = true;
                    }
                }
            }
        else{
            ChessSpot before = chessboardSquares[possibleMoves.get(0).r][possibleMoves.get(0).c];
            for (int i = 1; i < possibleMoves.size(); i++) {
                if(possibleMoves.get(i).r == b.row && possibleMoves.get(i).c == b.col)
                {
                    int newR = possibleMoves.get(i).r, newC = possibleMoves.get(i).c;
                    if(piece[newR][newC].toLowerCase().intern() == "king")
                    {
                        GAME_FINISHED = true;
                    }

                    occupied[newR][newC] = true;
                    occupied[before.row][before.col] = false;

                    piece[newR][newC] = piece[before.row][before.col];
                    piece[before.row][before.col] = "";

                    white[newR][newC] = white[before.row][before.col];
                    white[before.row][before.col] = false;

                    String key = (white[newR][newC]?"white":"black") + piece[newR][newC];
                    chessboardSquares[newR][newC].setIcon(new ImageIcon(images.get(key)));
                    chessboardSquares[before.row][before.col].setIcon(null);
                    movesPlayed++;
                    msg.setText(movesPlayed % 2 == 0?"White Turn":"Black Turn");
                    if(GAME_FINISHED) {
                        endTheGame(white[newR][newC]);
                    }

                    if(!GAME_FINISHED && piece[newR][newC].toLowerCase().intern() == "pawn" && (newR == 0 || newR == 7))
                    {
                        String newPiece = promotion();
                        String name = (white[newR][newC]?"white":"black") + newPiece;
                        Image image = images.get(name);
                        chessboardSquares[newR][newC].setIcon(new ImageIcon(image));
                        piece[newR][newC] = newPiece;
                    }

                    break;
                }
            }
            possibleMoves = new ArrayList<>();
            resetHighlight();
            onSelection = false;
        }
    }

    private String promotion(){
        JDialog.setDefaultLookAndFeelDecorated(true);
        String defaultSelect = "Queen";
        return JOptionPane.showInputDialog(null, "What do you want to promote to?", "Promotion", JOptionPane.QUESTION_MESSAGE, null, promotionPieces, defaultSelect).toString();
    }

    private void endTheGame(boolean whoWins){
        msg.setText("");
        CHECK.setText("");
        JOptionPane.showMessageDialog(frame, (whoWins ? "White" : "Black") + " wins!");
    }

    private boolean underCheck(boolean white, boolean[][] whites, List<Point> toCheck){
        for(Point p : toCheck){
            if(piece[p.r][p.c].toLowerCase().intern() == "king" && whites[p.r][p.c] != white)
                return true;
        }
        return false;
    }

    private void highlightBoxes(List<Point> arr){
//        System.out.println("highlightBoxes " + arr);
        for (int i = 0; i < arr.size(); i++) {
            Point pt = arr.get(i);
            ChessSpot toHighlight = chessboardSquares[pt.r][pt.c];
            toHighlight.setBorderPainted(true);
//            toHighlight.setBackground(Color.RED);
        }
    }

    private void resetHighlight(){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessboardSquares[i][j].setBorderPainted(false);
//                if ((i * 8 + j + (i) % 2) % 2 == 0) {
//                    chessboardSquares[i][j].setBackground(Color.WHITE);
//                } else {
//                    chessboardSquares[i][j].setBackground(Color.BLUE);
//                }
            }
        }
    }

    private void initGUI() {
        JToolBar toolbar = new JToolBar();
        gui.add(toolbar, BorderLayout.PAGE_START);
        toolbar.add(newButton);
        toolbar.addSeparator();
        toolbar.add(resignButton);
        toolbar.addSeparator();
        toolbar.add(msg);
        toolbar.add(CHECK);

        chessboard = new JPanel(new GridLayout(0, 9));
        chessboard.setBorder(new LineBorder(Color.PINK));
        gui.add(chessboard);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                final ChessSpot b = new ChessSpot(i, j);
                b.setIcon(new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)));
                if ((i * 8 + j + (i) % 2) % 2 == 0) {
                    b.setBackground(Color.GRAY);
                } else {
                    b.setBackground(Color.BLUE);
                }
                b.setOpaque(true);
                b.setBorderPainted(false);
                chessboardSquares[i][j] = b;
                b.addActionListener(e -> {
                    if(!GAME_FINISHED)
                        doWhenClicked(b);
                });
            }
        }

        chessboard.add(new JLabel(""));

        for (char ch : "ABCDEFGH".toCharArray())
            chessboard.add(new JLabel(ch + "", SwingConstants.CENTER));

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                switch (j) {
                    case 0:
                        chessboard.add(new JLabel(i + 1 + "", SwingConstants.CENTER));
                    default:
                        chessboard.add(chessboardSquares[i][j]);
                }
            }
        }
    }



    class ChessSpot extends JButton{

        int row, col;
        boolean moved;
        public ChessSpot(int r, int c){
            row = r;
            col = c;
            moved = false;
        }
    }
}
class GameLogic{

    private void displayMatrix(boolean[][] tmp){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(tmp[i][j]?"T":"F");
            }
            System.out.println();
        }
    }

    public static List<Point> getPawnMoves(int r, int c, boolean[][] occupied, boolean[][] white){
        List<Point> possible = new ArrayList<>();
        int dr = 1;
        int checkR = 1;
        if(white[r][c]){
            dr = -1;
            checkR = 6;
        }

        int tmpR = r;
        for (int i = 0; i < (checkR == r ?2:1);i++) {
            tmpR += dr;
            if(occupied[tmpR][c]){
                break;
            }
            possible.add(new Point(tmpR, c));
        }
        possible.addAll(getPawnAttacks(r, c, occupied, white, true));
        return possible;
    }

    public static List<Point> getPawnAttacks(int r, int c, boolean[][] occupied, boolean[][] white, boolean needOccupy){
        List<Point> possible = new ArrayList<>();
        int dr = white[r][c]?-1:1;
        for (int dc = -1; dc <= 1; dc+=2) {
            try {
                if(needOccupy) {
                    if (occupied[r + dr][c + dc] && white[r][c] != white[r + dr][c + dc])
                        possible.add(new Point(r + dr, c + dc));
                }
                else {
                    possible.add(new Point(r + dr, c + dc));
                }

            } catch (Exception e){ }
        }
        return possible;
    }

    public static List<Point> getRookMoves(int r, int c, boolean[][] occupied, boolean[][] white, int limit, boolean includeOwn){
        int[] dr = {1, 0, -1, 0};
        int[] dc = {0, 1, 0, -1};
        List<Point> arr = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            helper(r + dr[i], c + dc[i], dr[i], dc[i], occupied, white, white[r][c], arr, limit, 1, includeOwn);
        }
        return arr;
    }

    private static void helper(int r, int c, int dr, int dc, boolean[][] occupied, boolean[][] white, boolean isWhite, List<Point> arr, int limit, int count, boolean includeOwn){
        if(r < 0 || r >= 8 || c < 0 || c >= 8)
            return;
        if(count > limit) {
            return;
        }
        if(occupied[r][c] && white[r][c] != isWhite)
        {
            arr.add(new Point(r, c));
            return;
        }
        if(occupied[r][c] && white[r][c] == isWhite) {
            if(includeOwn)
                arr.add(new Point(r, c));
            return;
        }
        helper(r + dr, c + dc, dr, dc, occupied, white, isWhite, arr, limit, count + 1, includeOwn);
        arr.add(new Point(r, c));
    }

    public static List<Point> getBishopMoves(int r, int c, boolean[][] occupied, boolean[][] white, int limit, boolean includeOwn){
        int[] dr = {1, 1, -1, -1};
        int[] dc = {1, -1, 1, -1};
        List<Point> arr = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            helper(r + dr[i], c + dc[i], dr[i], dc[i], occupied, white, white[r][c], arr, limit, 1, includeOwn);
        }
        return arr;
    }

    public static List<Point> getQueenMoves(int r, int c, boolean[][] occupied, boolean[][] white, int limit, boolean includeOwn){
        List<Point> arr = getBishopMoves(r, c, occupied, white, limit, includeOwn);
        arr.addAll(getRookMoves(r, c, occupied, white, limit, includeOwn));
        return arr;
    }

    public static List<Point> getKingMoves(int r, int c, boolean[][] occupied, boolean[][] white, String[][] pieces){
        List<Point> initial = getQueenMoves(r, c, occupied, white, 1, false);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(white[r][c] != white[i][j]){
                    switch (pieces[i][j]){
                        case "Rook": initial.removeAll(getRookMoves(i, j, occupied, white, 26236, true)); break;
                        case "Pawn": initial.removeAll(getPawnAttacks(i, j, occupied, white, false)); break;
                        case "Bishop": initial.removeAll(getBishopMoves(i, j, occupied, white, 26236, true)); break;
                        case "Queen": initial.removeAll(getQueenMoves(i, j, occupied, white, 26236, true)); break;
                        case "Knight": initial.removeAll(getKnightMoves(i, j, occupied, white, true)); break;
                    }
                }
            }
        }
        return initial;
    }

    public static List<Point> getKnightMoves(int r, int c, boolean[][] occupied, boolean[][] white, boolean includeOwn){
        List<Point> arr = new ArrayList<>();
        int[] dr = {-1, -2, -2, -1, 1, 2, 2, 1};
        int[] dc = {-2, -1, 1, 2, 2, 1, -1, -2};
        for (int i = 0; i < 8; i++) {
            try{
                if(occupied[r + dr[i]][c + dc[i]] && white[r][c] == white[r + dr[i]][c + dc[i]]) {
                    if (includeOwn)
                        arr.add(new Point(r + dr[i], c + dc[i]));
                    continue;
                }
                arr.add(new Point(r + dr[i], c + dc[i]));
            }catch (Exception e){}
        }
        return arr;
    }

}

class Point{
    int r, c;
    public Point(int r, int c){
        this.r = r;
        this.c = c;
    }
    public boolean equals(Object other){
        if(other instanceof Point) {
            Point tmp = (Point)other;
            return r == tmp.r && c == tmp.c;
        }
        return false;
    }
    public String toString(){
        return String.format("(%d, %d)", r, c);
    }
}
