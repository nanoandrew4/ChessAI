/*
    Board class for ChessAI.
    Contains all elements of a chess game.
    
    TODO:
    Can take pieces while castling
    Can castle with other pieces so long as bool is false
*/

package chessai;

public final class Board
{
    //vars
    Piece[][] board = new Piece[8][8];
    boolean[][] hasMoved = new boolean[3][2]; //rook, king, rook (0 is black, 1 is white on 2nd dimension)
    private Move[] moves = new Move[128]; //will be used for undoing moves, needs to account for taking pieces
    int turnCount = 0;

    //default constructor
    Board() {
        createBoard();
        for(int x = 0; x < 3; x++)
            for(int y = 0; y < 2; y++)
                hasMoved[x][y] = false;
        for(int i = 0; i < 128; i++) moves[i] = new Move();
    }
    
    //show a console version of the board
    public void displayBoard(Piece[][] board) {
        System.out.print("\n    a   b   c   d   e   f   g   h\n");
        
        for(int y = 0; y < 8; y++) {
            System.out.print("  ---------------------------------\n" + (8 - y) + " |"); //17
            
            for (int x = 0; x < 8; x++) {
                switch (board[x][y].color) {
                    case WHITE: System.out.print("w"); break;
                    case BLACK: System.out.print("b"); break;
                    case NONE: System.out.print(" "); break;
                }
                System.out.print(board[x][y].name + " |");
            }
            System.out.print("\n");
        }
        System.out.print("  ---------------------------------\n");
    }
    
    //create the initial position
    public void createBoard() {
		
        Color color; color = Color.BLACK;
		
        for(int x = 0; x < 8; x++){
            for(int y = 2; y < 6; y++){
                board[x][y] = new Empty();
            }
        }

        int y = 0;

        for(int x = 0; x < 2; x++) {
            if(x == 1) color = Color.WHITE;

            board[0][y] = new Rook(color);
            board[7][y] = new Rook(color);

            board[1][y] = new Knight(color);		
            board[6][y] = new Knight(color);

            board[2][y] = new Bishop(color);
            board[5][y] = new Bishop(color);

            board[3][y] = new Queen(color);
            board[4][y] = new King(color);
            if(y == 0) y++;
            if(y == 7) y--;
            for(int a = 0; a < 8; a++){
                board[a][y] = new Pawn(color);
            }
            
            y = 7;
        }
    }

    //check for legality
    public boolean isLegal(Move m) {

            //initial universal checks
    	
    	System.out.println("Is legal starts");
        
        //type check
        if(m.piece.name == ' ') return false;
        if(board[m.sx][m.sy] != m.piece) return false;
        
        //bounds check
        if(m.sx > 7 || m.sx < 0 || m.sy > 7 || m.sy < 0 || m.ex > 7 || m.ex < 0 || m.ey > 7 || m.ey < 0)
            return false;
        
        //color check
        if(board[m.sx][m.sy].color != (turnCount % 2 == 0 ? Color.WHITE : Color.BLACK)) 
            return false;
        
        //destination is own color piece, also prevents same square movement
        if(board[m.ex][m.ey].color == (turnCount % 2 == 0 ? Color.WHITE : Color.BLACK))
            return false;
        
        //piece specific checks
        switch (board[m.sx][m.sy].name) {
            //pawn
            case 'P':
                //find change in y, inverse if black's turn. should always be positive
                int dy = (m.sy - m.ey) * (turnCount % 2 == 0 ? 1 : -1);
                
                //moving up
                switch (dy) { 
                    case 1:
                        //taking pieces
                        if(Math.abs(m.sx - m.ex) == 1) {
                        	Move lm = moves[turnCount-1]; //reference for readability
                        	if(turnCount > 0 && lm.piece.name == 'P' 
                        	&& Math.abs(lm.ey - lm.sy) == 2 
                        	&& (lm.piece.color == (turnCount % 2 == 0 ? Color.BLACK : Color.WHITE))
                        	&& lm.ex == m.ex && lm.ey == m.sy){
                        		break;
                        	}
                        	else if(board[m.ex][m.ey].name == ' ')
                                return false;
                        }
                        //single move up
                        else if (m.sx == m.ex && board[m.ex][m.ey].name == ' ')
                            break;
                        else return false;
                        break;
                    case 2: //double move forwards
                        //check for in initial position
                        if(!((turnCount % 2 == 0 && m.sy == 6) || (turnCount % 2 == 1 && m.sy == 1)) || board[m.ex][m.ey].name != ' ')
                            return false;
                        break;
                    default: return false; //any greater magnitude of dy is illegal
                }
                break;
            //rook
            case 'R':
                //disallow trying to move in both dimensions
                if(m.ey - m.sy != 0 && m.ex - m.sx != 0)
                    return false;
                break;
            //knight
            case 'N':
                //knight moves 2 spaces in one direction, and 1 square in the other
                if(!((Math.abs(m.ex - m.sx) == 1 && Math.abs(m.ey - m.sy) == 2) || 
                        (Math.abs(m.ex - m.sx) == 2 && Math.abs(m.ey - m.sy) == 1)))
                    return false;
                break;
            //bishop
            case 'B':
                //change in x and y are the same
                if(Math.abs(m.ex - m.sx) != Math.abs(m.ey - m.sy))
                    return false;
                break;
            //queen
            case 'Q':
                //checks of both bishop and rook
                if((m.ey - m.sy != 0 && m.ex - m.sx != 0) && 
                        (Math.abs(m.ex - m.sx) != Math.abs(m.ey - m.sy)))
                    return false;
                break;
            //king
            case 'K':
                //castling. if legal, only check moving through other pieces later
            	if(m.ex - m.sx == 2 && m.sy == m.ey) { //castling kingside
                    if(m.piece.color == Color.WHITE && !hasMoved[2][1] && !hasMoved[1][1])
                        break;
                    else if(m.piece.color == Color.BLACK && !hasMoved[2][0] && !hasMoved[1][0])
                        break;
                    else return false;
            	}
                else if(m.sx - m.ex == 2 && m.sy == m.ey) { //castling queenside, extra check
                    if(m.piece.color == Color.WHITE && !hasMoved[1][1] && !hasMoved[0][1] && board[1][m.sy].name == ' ')
                        break;
                    else if(m.piece.color == Color.BLACK && !hasMoved[1][0] && !hasMoved[0][0] && board[1][m.sy].name == ' ')
                        break;
                    else return false;
            	}
            	//if not castling, directional movement <= 1
                if(Math.abs(m.ex - m.sx) > 1 || Math.abs(m.ey - m.sy) > 1)
                    return false;
                break;
            default: return false;
        }
        
        //moving through pieces
        if(board[m.sx][m.sy].name != 'N') { //not for knights
           for(int x = m.sx, y = m.sy; x != m.ex || y != m.ey;) {
                if(x != m.ex) x += (m.sx < m.ex ? 1 : -1);
                if(y != m.ey) y += (m.sy < m.ey ? 1 : -1);

                if(board[x][y].name != ' ' && !(x == m.ex && y == m.ey))
                    return false;
            } 
        }
        
        //player's king in check
        makeMove(m);
        if(isInCheck(turnCount % 2 == 0 ? Color.BLACK : Color.WHITE))
        {
            undoMove();
            return false;
        }
        else undoMove();

        // if has not returned false before here, must be legal
        return true;
    }
    
    //check for checkmate
    public boolean checkmated() {
        //find the king
    	for(int x = 0; x < 8; x++) {
            for(int y = 0; y < 8; y++) {
                if(board[x][y].name == 'K' && board[x][y].color == (turnCount % 2 == 0 ? Color.WHITE : Color.BLACK)){
                    // check 3x3 square surrounding king to see if any legal moves remain
                    for(int ex = x - 1; ex < x + 2; ex++) {
                        for(int ey = y - 1; ey < y + 2; ey++) {
                            if(ex >= 0 && ex < 8 && ey >= 0 && ey < 8) { // prevents arrayOutOfBounds
                                if(isLegal(new Move(x, ex, y, ey, board[x][y], board[ex][ey]))) {
                                    return false; // if any legal move is found, not checkmate
                                }
                            }
                        }
                    }
                    x = 8;
                    break;
                }
            }
    	}
    	return true;
    }
    
    //check for king in check
    public boolean isInCheck(Color c) {
        //find the king
    	for(int x = 0; x < 8; x++) {
            for(int y = 0; y < 8; y++) {
                if(board[x][y].name == 'K' && board[x][y].color == c){
                        //check all possible angles of attack
                    //left
                    for(int i = x - 1; i >= 0; i--)
                        if(board[i][y].name != ' ')
                            if(isLegal(new Move(i, x, y, y, board[i][y], board[x][y])))
                                return true;
                            else break;
                    //right
                    for (int i = x + 1; i < 8; i++)
                        if(board[i][y].name != ' ')
                            if(isLegal(new Move(i, x, y, y, board[i][y], board[x][y])))
                                return true;
                            else break;
                    //up
                    for(int i = y - 1; i >= 0; i--)
                        if(board[x][i].name != ' ')
                            if(isLegal(new Move(x, x, i, y, board[x][i], board[x][y])))
                                return true;
                            else break;
                    //down
                    for(int i = y + 1; i < 8; i++)
                        if(board[x][i].name != ' ')
                            if(isLegal(new Move(x, x, i, y, board[x][i], board[x][y])))
                                return true;
                            else break;
                    //northwest
                    for(int i = x - 1, j = y - 1; i >= 0 && j >= 0; i--, j--)
                        if(board[i][j].name != ' ')
                            if(isLegal(new Move(i, x, j, y, board[i][j], board[x][y])))
                                return true;
                            else break;
                    //northeast
                    for(int i = x + 1, j = y - 1; i < 8 && j >= 0; i++, j--)
                        if(board[i][j].name != ' ')
                            if(isLegal(new Move(i, x, j, y, board[i][j], board[x][y])))
                                return true;
                            else break;
                    //southwest
                    for(int i = x - 1, j = y + 1; i >= 0 && j < 8; i--, j++)
                        if(board[i][j].name != ' ')
                            if(isLegal(new Move(i, x, j, y, board[i][j], board[x][y])))
                                return true;
                            else break;
                    //southeast
                    for(int i = x + 1, j = y + 1; i < 8 && j < 8; i++, j++)
                        if(board[i][j].name != ' ')
                            if(isLegal(new Move(i, x, j, y, board[i][j], board[x][y])))
                                return false;
                            else break;

                    ///knights

                    x = 8; break;
                }
            }
    	}
    	return true;
    }
    
    //make a move, hopefully after checking legality
    public void makeMove(Move m) {
    	//special moves
        
    	//castling
    	if(m.piece.name == 'K') {
    		hasMoved[1][(turnCount % 2 == 0 ? 0 : 1)] = true;
            if(m.ex - m.sx == 2) { //kingside
                // place rook to left of king
                swap(m.ex-1, 7, m.sy, m.sy);
            }
            if(m.sx - m.ex == 2) { //queenside
                // place rook to right of king
                swap(m.ex+1, 0, m.sy, m.sy);
            }
    	}
    	else if(m.piece.name == 'R'){
    		// if a rook is moved, set hasMoved to true to prevent castling
    		hasMoved[(m.sx == 7 ? 2 : 0)][(turnCount % 2 == 0 ? 1 : 0)] = true;
    	}
    	else if(m.piece.name == 'P' && Math.abs(m.ex - m.sx) == 1 && Math.abs(m.ey - m.sy) == 1
    				&& board[m.ex][m.ey].name == ' '){ // en passant
    		board[m.ex][m.sy] = new Empty();
    	}
    	
    	//move pieces
        board[m.ex][m.ey] = board[m.sx][m.sy];
        board[m.sx][m.sy] = new Empty();
    	
        moves[turnCount] = m; //does not store castling
        turnCount++;
    }
    
    //undo a move by accessing moves stack, decrease turn count
    public void undoMove() {
        //if a move has been made
        if (turnCount > 0) {
        	turnCount--;
        	Move m = moves[turnCount];
            board[m.sx][m.sy] = board[m.ex][m.ey];
            board[m.ex][m.ey] = m.pieceCaptured;
            if(m.piece.name == 'P' && m.pieceCaptured.name == ' ' && (m.ex != m.sx)){
            	board[m.ex][m.sy] = new Pawn((turnCount % 2 == 1 ? Color.WHITE : Color.BLACK));
            }
            else if(m.piece.name == 'K' && m.ex - m.sx == 2){
            	System.out.println("Undo castling");
            	//swap(m.sx, m.ex, m.sy, m.ey);
            	hasMoved[1][(turnCount % 2 == 1 ? 1 : 0)] = false;
            	swap(5, 7, m.sy, m.ey);
            	hasMoved[2][(turnCount % 2 == 1 ? 1 : 0)] = false;
            }
            else if(m.piece.name == 'K' && m.ex - m.sx == -2){
            	System.out.println("Undo castling");
            	//swap(m.sx, m.ex, m.sy, m.ey);
            	hasMoved[1][(turnCount % 2 == 1 ? 1 : 0)] = false;
            	swap(0, 3, m.sy, m.ey);
            	hasMoved[0][(turnCount % 2 == 1 ? 1 : 0)] = false;
            }
        }
    }
    
    public void swap(int sx, int ex, int sy, int ey){
    	Piece tmp = board[sx][sy];
    	board[sx][sy] = board[ex][ey];
    	board[ex][ey] = tmp;
    }
}
