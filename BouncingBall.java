package BouncingBall;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

//Hailey Maimone -- Chat Program -- CET350 Technical Computing Using Java


public class BouncingBall extends Frame implements WindowListener, ComponentListener, ActionListener, AdjustmentListener, Runnable, MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 17L; // serial version ID

    // declare constants
    private final int BUTTONH = 20; // button height
    private final int MAXObj = 100; // maximum object size
    private final int MINObj = 10; // minimum object size
    private final int SPEED = 50; // initial speed
    private final int SBvisible = 10; // visible Scroll Bar
    private final int SBunit = 1; // Scroll Bar unit step size
    private final int SBblock = 10; // Scroll Bar block step size
    private final int SOBJ = 21; // initial object width
    private final int DELAY = 1; // timer delay constant

    // declare other variables
    private int WinTop = 10; // top of frame
    private int WinLeft = 10; // left side of frame
    private int BUTTONW = 50; // initial button width
    private Insets I; // insets of frame
    private int SObj = SOBJ; // initial object width
    private int SpeedSBmin = 1; // speed scrollbar minimum value
    private int SpeedSBmax = 100 + SBvisible; // speed scrollbar maximum value with visible offset
    private int SpeedSBinit = SPEED; // initial speed scrollbar value
    private int ScrollBarW; // scrollbar width
    private Ball ball; // object to draw
    private Label SPEEDL = new Label("Speed", Label.CENTER); // label for speed scroll bar
    private Label SIZEL = new Label("Size", Label.CENTER); // label for size scroll bar
    private boolean running; // boolean for run method
    private boolean TimePause; // boolean for pause mode
    private boolean started; // boolean for start mode
    private int speed; // int for scrollbar speed
    private int delay; // int for timer delay
    private int i;
    private boolean ok ;
    private Panel sheet = new Panel(); // panel for drawing canvas
    private Panel control = new Panel(); // panel for button area
    GridBagLayout gbl = new GridBagLayout(); // GridBagLayout for the control panel
    GridBagConstraints con = new GridBagConstraints();

    private Point FrameSize = new Point(640, 400); // initial frame size
    private Point Screen = new Point(FrameSize.x - 1, FrameSize.y - 1); // drawing screen size
    private Point m1 = new Point(0, 0); // first mouse point
    private Point m2 = new Point(0, 0); // second mouse point
    private Rectangle Perimeter = new Rectangle(0, 0, Screen.x, Screen.y); // bouncing perimeter
    private Rectangle db = new Rectangle(); // drag box rectangle
    Thread thethread; // thread for timer delay
    Scrollbar SpeedScrollBar, ObjSizeScrollBar; // scroll bars
    Button Start, Stop, Quit; // buttons

    BouncingBall() {
        MakeSheet(); // Determine the sizes for the sheet
        started = false; // start in pause mode

        try {
            initComponents(); // try to initialize the components
        } catch (Exception e) {
            e.printStackTrace();
        }
        SizeScreen();
        start();
    }
    public static void main(String[] args) {
        BouncingBall b = new BouncingBall(); // create an object
    }

    private void MakeSheet() { // gets the insets and adjusts the sizes of the items
        I = getInsets();
        Screen.x = FrameSize.x - I.left - I.right;
        Screen.y = FrameSize.y - I.top - I.bottom - 2 * BUTTONH;
        setSize(FrameSize.x, FrameSize.y);
        BUTTONW = Screen.y / 11; // determine the width of the buttons (11 units)
        ScrollBarW = 2 * BUTTONW; // determine the scroll bar width
        setBackground(Color.lightGray);
    }

    public void SizeScreen() {
        ball.setBounds(I.left, I.top, Screen.x, Screen.y);
    }

    public void initComponents() throws Exception, IOException {
        delay = DELAY;
        Start = new Button("Run"); // create the start button
        Stop = new Button("Pause"); // create the stop button
        Quit = new Button("Quit"); // create the quick button
        Start.setEnabled(false); // do we start in run mode or pause mode?
        Stop.setEnabled(true);

        SpeedScrollBar = new Scrollbar(Scrollbar.HORIZONTAL); // create the speed scroll bar
        SpeedScrollBar.setMaximum(SpeedSBmax); // set the max speed
        SpeedScrollBar.setMinimum(SpeedSBmin); // set the min speed
        SpeedScrollBar.setUnitIncrement(SBunit); // set the unit increment
        SpeedScrollBar.setBlockIncrement(SBblock); // set the block increment
        speed = (SpeedSBmax + SpeedSBmin - SpeedSBinit) - 10; // reverse scrollbar values so that left decreases and right increases
        delay = speed / DELAY; // set delay to be speed divided by delay constant
        SpeedScrollBar.setValue(SpeedSBinit); // set the initial value
        SpeedScrollBar.setVisibleAmount(SBvisible); // set the visible size
        SpeedScrollBar.setBackground(Color.gray); // set the background color
        ObjSizeScrollBar = new Scrollbar(Scrollbar.HORIZONTAL); // create the size scroll bar
        ObjSizeScrollBar.setMaximum(MAXObj); // set the max speed
        ObjSizeScrollBar.setMinimum(MINObj); // set the min speed
        ObjSizeScrollBar.setUnitIncrement(SBunit); // set the unit increment
        ObjSizeScrollBar.setBlockIncrement(SBblock); // set the block increment
        ObjSizeScrollBar.setValue(SOBJ); // set the initial value
        ObjSizeScrollBar.setVisibleAmount(SBvisible); // set the visible size
        ObjSizeScrollBar.setBackground(Color.gray); // set the background color
        SpeedScrollBar.addAdjustmentListener(this); // add the speed scroll bar listener
        ObjSizeScrollBar.addAdjustmentListener(this); // add the size scroll bar listener

        m1.setLocation(0, 0);
        m2.setLocation(0, 0);
        Perimeter.setBounds(0, 0, Screen.x, Screen.y);
        Perimeter.grow(-1, -1);
        setLayout(new BorderLayout()); // layout border
        setBounds(WinLeft, WinTop, FrameSize.x, FrameSize.y);
        setBackground(Color.lightGray);
        setVisible(true);
        setTitle("Group 1 - Bouncing Ball Program");

        // setup GridBagLayout for button area
        control.setLayout(gbl);
        con.gridx = 0;
        con.gridy = 0;
        con.weightx = 0.5;
        con.weighty = 0.5;
        con.ipadx = ScrollBarW / 2;
        con.anchor = GridBagConstraints.LINE_START;
        con.insets = new Insets(0, 30, 0, 0);
        gbl.setConstraints(SpeedScrollBar, con);
        control.add(SpeedScrollBar);
        con.gridx = GridBagConstraints.RELATIVE;
        con.anchor = GridBagConstraints.CENTER;
        con.ipadx = BUTTONW / 2;

        con.insets = new Insets(0, 90, 0, 0);
        con.weightx = 0;
        con.weighty = 0;
        gbl.setConstraints(Start, con);
        control.add(Start);
        con.insets = new Insets(0, 0, 0, 40);
        gbl.setConstraints(Stop, con);
        control.add(Stop);
        con.insets = new Insets(0, -40, 0, 90);
        gbl.setConstraints(Quit, con);
        control.add(Quit);
        con.weightx = 0.5;
        con.weighty = 0.5;
        con.anchor = GridBagConstraints.LINE_END;
        con.ipadx = ScrollBarW / 2;
        con.insets = new Insets(0, 0, 0, 30);
        gbl.setConstraints(ObjSizeScrollBar, con);
        control.add(ObjSizeScrollBar);
        con.gridx = 0;
        con.gridy = 1;
        con.anchor = GridBagConstraints.LINE_START;
        con.insets = new Insets(0, 30, 0, 0);
        gbl.setConstraints(SPEEDL, con);
        control.add(SPEEDL);
        con.anchor = GridBagConstraints.LINE_END;
        con.gridx = 4;
        con.insets = new Insets(0, 0, 0, 30);
        gbl.setConstraints(SIZEL, con);
        control.add(SIZEL);



        control.setBackground(Color.lightGray);
        control.setSize(FrameSize.x, 2 * BUTTONH); // size control
        control.setVisible(true);

        sheet.setLayout(new BorderLayout(0, 0)); // sheet border layout;
        ball = new Ball(SObj, Screen); // create with a ball size and drawing size
        ball.setBackground(Color.white);
        sheet.add("Center", ball);
        sheet.setVisible(true); // make the layout visible
        add("Center", sheet); // add sheet panel to center of BorderLayout
        add("South", control); // add control panel to south of BorderLayout

        Start.addActionListener(this); // add the start button listener
        Stop.addActionListener(this); // add the stop button listener
        Quit.addActionListener(this); // add the quit button listener
        ball.addMouseMotionListener(this); // add mouse motion listener
        ball.addMouseListener(this); // add mouse listener
        this.addComponentListener(this); // add the component listener
        this.addWindowListener(this); // add the window listener
        TimePause = false;
        running = true;
        validate(); // validate the layout
    }

    public void start() {
        if (thethread == null) { // create a thread if it does not exist
            thethread = new Thread(this); // create a new thread
            thethread.start(); // start the thread
            ball.repaint();
        }
    }

    public void stop() {
        // set running flag to false, interrupt the thread
        // remove all listeners and exit
        running = false;
        thethread.interrupt();
        Start.removeActionListener(this);
        Quit.removeActionListener(this);
        SpeedScrollBar.removeAdjustmentListener(this);
        ObjSizeScrollBar.removeAdjustmentListener(this);
        ball.removeMouseMotionListener(this);
        ball.removeMouseListener(this);
        this.removeComponentListener(this);
        this.removeWindowListener(this);
        dispose();
        System.exit(0);
    }

    public void run() {
        while (running) {
            if (!TimePause) { // if the program isn't paused
                started = true; // then the animation is started
                ball.Size(); // apply new size
                try {
                    Thread.sleep(delay); // try to sleep the thread for the new speed delay
                } catch (InterruptedException e) {

                }
                ball.repaint(); // force a repaint
                ball.collisionSide(); // check for a collision
                ball.move(); // move the object
            }
            try {
                Thread.sleep(1); // try to sleep the thread for 1 ms so that the loop
                                        // has a chance to be interrupted
            } catch (InterruptedException exception) {

            }
        }
    }

    public void checkObjSize() {
        int x = ball.getX(); // get current object x
        int y = ball.getY(); // get current object y
        int obj = ball.getObjSize(); // get current object size
        int right = x + (obj - 1 / 2) + 1; // right-side object calculation
        int bottom = y + (obj - 1 / 2) + 1; // bottom-side object calculation
        if (right > Screen.x) { // check if object will leave right-side of screen
            ball.setX(Screen.x - (obj - 1 / 2) - 2); // if it does, reposition x
        }

        if (bottom > Screen.y) { // check if object will leave bottom of screen
            ball.setY(Screen.y - (obj - 1/ 2) - 2); // if it does, reposition y
        }
    }
    public void componentResized(ComponentEvent e) {
        FrameSize.x = getWidth(); // get current frame width
        FrameSize.y = getHeight(); // get current frame height

        Rectangle r = new Rectangle(); // create new rectangle called r
        Rectangle b = new Rectangle(ball.getX() - (SObj - 1) / 2, ball.getY() - (SObj - 1) / 2, SObj, SObj);
        // create a rectangle copy of the ball
        int mr = 0; // maximum right integer
        int mb = 0; // maximum bottom integer
        MakeSheet(); // make the sheet
        I = sheet.getInsets(); // get the canvas' insets
        checkObjSize(); // make sure the object will be inside of the screen on resize
        SizeScreen(); // size the canvas
        I = getInsets(); // get the frame's insets
        if (ball.getWallSize() != 0) { // as long as the rectangle vector isn't empty
            r.setBounds(ball.getOne(0)); // get 0th rectangle
            mr = r.x + r.width; // initialize max right
            mb = r.y + r.height; // initialize max bottom

            for (int i = 0; i < ball.getWallSize(); i++) {
                r.setBounds(ball.getOne(i)); // get ith rectangle
                mr = Math.max((r.x + r.width), mr); // keep max right
                mb = Math.max((r.y + r.height), mb); // keep max bottom
            }

            r.setBounds(b); // process the ball
            mr = Math.max((r.x + r.width), mr); // keep max right
            mb = Math.max((r.y + r.height), mb); // keep max bottom
            if (mr > Screen.x || mb > Screen.y) { // if max right or max bottom is greater than current screen width or screen height
                setSize(Math.max((mr + 5), Screen.x) + I.left + I.right, Math.max((mb + 5), Screen.y) + I.top + I.bottom + 2 * BUTTONH);
                // set the new frame size
                setExtendedState(ICONIFIED); // set extended state iconified
                setExtendedState(NORMAL); // set extended state normal
            }
        }
        Screen.setLocation(sheet.getWidth() - 1 , sheet.getHeight() - 1); // update the screen point
        Perimeter.setBounds(ball.getBounds()); // update the perimeter rectangle
        Perimeter.grow(-1, -1); // shrink the perimeter rectangle by -1 all around
        ball.reSize(Screen); // resize the ball screen
        ball.repaint(); // repaint
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == Start) { // if start button is clicked
            if (Start.getLabel() == "Run") {
                TimePause = false; // set run mode flag
                Start.setEnabled(false); // disable start button
                Stop.setEnabled(true); // enable stop button
            }
            thethread.interrupt(); // interrupt the thread
        }

        if (source == Stop) { // if pause button is clicked
            if (Stop.getLabel() == "Pause") {
                TimePause = true; // set pause mode flag
                Start.setEnabled(true); // enable start button
                Stop.setEnabled(false); // disable stop button
            }
            thethread.interrupt(); // interrupt the thread
        }

        if (source == Quit) { // if quit button is clicked
            stop(); // stop the program and exit
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        int TS; // integer for size scrollbar value
        int half;

        Scrollbar sb = (Scrollbar)e.getSource(); // get the scrollbar that triggered the event
        if (sb == SpeedScrollBar) {
            System.out.println(sb.getValue());
            speed = (SpeedSBmax + SpeedSBmin - sb.getValue()) - 10; // reverse scrollbar values so that left decreases and right increases
            delay = speed / DELAY; // set delay to be speed divided by delay constant
            thethread.interrupt(); // interrupt the thread so that the new delay can be applied
            System.out.println("Speed value: " + sb.getValue());

        }

        if (sb == ObjSizeScrollBar) {
            TS = e.getValue(); // get the value
            TS = (TS / 2) * 2 + 1; // Make odd to account for center position
            half = (TS - 1) / 2; // half the size of the ball
            Rectangle t;
            Rectangle b = new Rectangle(ball.getX() - half - 1, ball.getY() - half - 1, TS + 2, TS + 2);
            // rectangle copy of the ball
            if (b.equals(Perimeter.intersection(b))) { // if the ball is inside of the perimeter
                i = 0;
                ok = true;
                while ((i < ball.getWallSize()) && ok) {
                    t = ball.getOne(i); // get a rectangle from the vector
                    if (t.intersects(b)) { // if the rectangle intersects with the ball's new size
                        ok = false; // set boolean to false
                    } else { // otherwise increment
                        i++; // increment i
                    }
                }
                if (ok && ball.checkSize(TS)) { // if no intersection with the new bal size
                    ball.newSize(TS); // set new ball size
                    ball.Size(); // size the ball
                } else if (!ok) { // if there is an intersection with the new ball size
                    sb.setValue(ball.getObjSize()); // set the scrollbar back to it's previous value
                }
            } else {
                sb.setValue(ball.getObjSize()); // if not, set scrollbar back to its previous value
            }
        }
        System.out.println("Size value: " + sb.getValue());
        ball.repaint(); // force a repaint
    }

    public void componentHidden(ComponentEvent e) {

    }

    public void componentShown(ComponentEvent e) {

    }

    public void componentMoved(ComponentEvent e) {

    }

    public void windowClosing(WindowEvent e) {
        stop();
    }

    public void windowClosed(WindowEvent e) {

    }

    public void windowOpened(WindowEvent e) {

    }

    public void windowActivated(WindowEvent e) {

    }

    public void windowDeactivated(WindowEvent e) {

    }

    public void windowIconified(WindowEvent e) {

    }

    public void windowDeiconified(WindowEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
        Point p = new Point(e.getX(), e.getY()); // get the current mouse clicked point
        Rectangle b;
        i = 0;
        while ((i < ball.getWallSize())) {
            b = ball.getOne(i); // get a vector rectangle
            if (b.contains(p)) { // if the rectangle contains the clicked point
                ball.removeOne(i); // delete it from the vector
            } else { // otherwise
                i++; // increment i
            }
        }
        ball.repaint(); // force a repaint
    }

    public void mousePressed(MouseEvent e) {
        m1.setLocation(e.getPoint()); // set mouse point 1 location to current pressed location
    }

    public void mouseReleased(MouseEvent e) {
        Rectangle b = new Rectangle(ball.getX() - (SObj - 1) / 2, ball.getY() - (SObj - 1) / 2, SObj, SObj);
        // create a rectangle copy of the ball
        b.grow(1, 1); // grow the ball by 1 all around
        Rectangle ZERO = new Rectangle(0, 0, 0, 0); // create a zero rectangle
        Rectangle r = getDragBox(e); // get the current drawn rectangle
        Rectangle vect; // rectangle representing a rectangle from the vector
        i = 0; // index integer
        boolean store = true; // boolean representing if we should store the drawn rectangle

        if (!Perimeter.contains(r)) { // if the rectangle leaves the screen
            r = Perimeter.intersection(r); // push it back within the perimeter
        }
        if (r.intersects(b)) { // if the rectangle intersects the ball
            store = false; // we won't stop the rectangle in the vector
        }

        while ((i < ball.getWallSize()) && store) {
            vect = ball.getOne(i); // get a rectangle from the vector
            if (r.intersection(vect).equals(r)) { // if the new rectangle is covered by any rectangle in the vector
                store = false; // don't store the new rectangle
            }
            if (r.intersection(vect).equals(vect)) { // if the new rectangle covers any rectangle in the vector
                ball.removeOne(i); // delete that rectangle from the vector
            } else { // otherwise
                i++; // increment i
            }
        }
        if (store) { // if the store boolean is true
            ball.addOne(r); // then we store the drawn rectangle
        }
        ball.setDragBox(ZERO); // delete the drawn rectangle because it either is covered by the stored rectangle
                                // or were not storing it so it should be removed anyways
        ball.repaint(); // force a repaint
    }

    public void mouseEntered(MouseEvent e) {
        ball.repaint(); // force a repaint
    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        db.setBounds(getDragBox(e)); // set db the bounds of the drawn rectangle
        if (Perimeter.contains(db)) { // if db is within the perimeter of the screen
            ball.setDragBox(db); // send it to the canvas to be drawn
            ball.repaint(); // force a repaint
        }
    }

    public Rectangle getDragBox(MouseEvent e) {
        Rectangle dragRect = new Rectangle(); // make a new rectangle
        m2.setLocation(e.getPoint()); // get second mouse point location
        // mathematical functions to determine dragbox coordinates to draw the rectangle
        int x = Math.min(m1.x, m2.x);
        int y = Math.min(m1.y, m2.y);
        int width = Math.max(m1.x - m2.x, m2.x - m1.x);
        int height = Math.max(m1.y - m2.y, m2.y - m1.y);
        dragRect.setBounds(x, y, width, height); // set the bounds of the drawn rectangle
        return dragRect; // return the rectangle
    }

    public void mouseMoved(MouseEvent e) {

    }
}

class Ball extends Canvas {
    private static final long serialVersionUID = 13L;
    private static final Rectangle ZERO = new Rectangle(0, 0, 0, 0); // zero rectangle
    private Point Screen; // point for height and width of screen
    private int SObj; // object size
    private int NewSize; // new object size
    private int x, y; // x and y integer
    private int xmin, xmax, ymin, ymax; // minimum and maximum x and y value integers
    private boolean right, down;
    private Vector<Rectangle> Walls = new Vector <Rectangle>();
    private Rectangle dbox = new Rectangle(ZERO); // create dbox rectangle and initially set it to zero rectangle
    private Image buffer; // create doublebuffering canvas
    private Graphics g; // create graphics g


    public Ball(int SB, Point res) {
        Screen = res; // set height and width to the passed in point
        SObj = SB; // set object size to passed in value
        NewSize = SObj; // set NewSize to equal objects size upon object creation
        minMax(); // set minimum and maximum allowed x and y values
        initialPos(); // randomize initial x and y position of the ball
        down = true; // set down flag to start at true
        right = true; // set right flag to start at true
    }

    public void initialPos() { // method to randomize the intiial position of the ball
        int xrange = (xmax - xmin) + 1;
        int yrange = ((ymax - ymin) + 1) - 80;
        int xrand = ((int)(Math.random() * xrange) + xmin);
        int yrand = ((int)(Math.random() * yrange) + ymin);
        x = xrand;
        y = yrand;
    }

    public void addOne(Rectangle r) {
        Walls.addElement(new Rectangle(r)); // add a rectangle to the vector
    }

    public void removeOne(int i) {
        Walls.removeElementAt(i); // remove a rectangle from the vector
    }

    public Rectangle getOne(int i) {
        return Walls.elementAt(i); // get a rectangle from the vector
    }

    public int getWallSize() {
        return Walls.size(); // get vector size
    }

    public int getObjSize() {
        return NewSize; // get current object size
    }

    public void setDragBox(Rectangle db) {
        dbox.setBounds(db.x, db.y, db.width, db.height); // set dbox rectangle to passed in rectangle's bounds
    }


    public Rectangle collisionDetect() {
        Rectangle r = new Rectangle(); // create new rectangle called r
        Rectangle z = new Rectangle(ZERO); // create an empty (0) rectangle
        Rectangle b = new Rectangle(x - (SObj - 1) / 2, y - (SObj - 1) / 2, SObj, SObj); // create a rectangle copy of the ball

        b.grow(1, 1); // grow the ball rectangle
        int i = 0; // initialize while loop index to 0
        boolean ok = true; // set ok to true
        while ((i < Walls.size()) && ok) {
            r = Walls.elementAt(i); // set r to the current rectangle in the vector
            if (r.intersects(b)) { // if the rectangle intersects with the ball
                ok = false; // set boolean to false
            } else { // otherwise
                i++; // increment the index
            }
        }
        if (!ok) { // if we found a collision
            return r; // return the rectangle that the ball collided with
        }
        return z; // otherwise return the zero rectangle
    }

    public void collisionSide() {
        for (int i = 0; i < Walls.size(); i++) { // loop through the vector of rectangles
            Rectangle b = new Rectangle(x - (SObj - 1) / 2, y - (SObj - 1) / 2, SObj, SObj); // create a rectangle copy of the ball
            Rectangle r = collisionDetect(); // create a rectangle that is the rectangle that collided with the ball or the zero rectangle
            Rectangle c = getOne(i); // get the current rectangle from the vector
            if (r == c) { // if the current vector rectangle and the rectangle that the ball collided with are the same (not the zero rectangle)
                int ballLeft = b.x + 1; // left side of ball
                int ballRight = (b.x + b.width); // right side of ball
                int ballTop = b.y; // top side of ball
                int ballBottom = (b.y + b.height); // bottom side of ball
                int rectLeft = r.x + 1; // left side of rectangle
                int rectRight = (r.x + r.width); // right side of rectangle
                int rectTop = r.y; // top side of rectangle
                int rectBottom = (r.y + r.height); // bottom side of rectangle

                if (ballRight <= rectLeft) { // if ball collided with left side of rectangle
                    right = false; // set right to false
                }
                if (ballLeft >= rectRight) { // if ball collided with right side of rectangle
                    right = true; // set right to true
                }
                if (ballTop >= rectBottom) { // if ball collided with bottom side of rectangle
                    down = true; // set down to true
                }
                if (ballBottom <= rectTop) { // if ball collided with top side of rectangle
                    down = false; // set down to false
                }
            }
        }
    }

    public void minMax() {
        xmin = (SObj / 2) + 1; // set minimum allowed x value
        ymin = (SObj / 2) + 1; // set minimum allowed y value
        xmax = Screen.x - (SObj / 2) - 1; // set maximum allowed x value
        ymax = Screen.y - (SObj / 2) - 1; // set maximum allowed y value
    }

    public void move() {
        if (!checkX()) { // if x value is not within allowed range
            right = !right; // complement right flag
        }
        if (!checkY()) { // if y value is not within allowed range
            down = !down; // complement down flag
        }
        if (right) { // if right flag is enabled
            x += 1; // increment x position by 1
        } else { // otherwise
            x -= 1; // decrement x position by 1
        }
        if (down) { // if down flag is enabled
            y += 1; // increment x position by 1
        } else { // otherwise
            y -= 1; // decrement x position by 1
        }
    }

    public void setX(int x) {
        this.x = x; // set objects current x position
    }

    public int getX() {
        return this.x; // returns objects current x position
    }

    public void setY(int y) {
        this.y = y; // sets objects current y position
    }

    public int getY() {
        return this.y; // returns objects current y position
    }

    public boolean checkX() {
        return x > xmin && x < xmax; // check if current x value is within allowed range
    }

    public boolean checkY() {
        return y > ymin && y < ymax; // check if current y value is within allowed range
    }

    public boolean checkSize(int NSObj) { // method to check if theoretical new size would fit in the screen
        int left = x - (NSObj / 2); // calculate left boundary in respect to the new object size
        int right = x + (NSObj / 2); // calculate right boundary in respect to the new object size
        int top = y - (NSObj / 2); // calculate top boundary in respect to the new object size
        int bottom = y + (NSObj / 2); // calculate bottom boundary in respect to the new object size
        return left > xmin && right < xmax && top > ymin && bottom < ymax;
    }


    public void newSize(int NS) {
        NewSize = NS; // set new size to passed value
        minMax(); // set allowable x and y value range
    }

    public void Size() {
        SObj = NewSize; // set object size to the new size
        minMax(); // set allowable x and y value range
    }

    public void reSize(Point res) {
        Screen = res; // set Screen to the passed in Point
        minMax(); // set allowable x and y value range
    }

    public void paint(Graphics cg) {
        buffer = createImage(Screen.x, Screen.y); // create offscreen image
        if (g != null) { // check if g exists
            g.dispose(); // if it does, remove it
        }
        g = buffer.getGraphics(); // s g to the offscreen image
        g.setColor(Color.blue); // set the color to blue
        g.drawRect(0, 0, Screen.x - 1, Screen.y - 1); // draw outline of rectangle

        g.setColor(Color.red); // set color of solid circle below to red
        g.fillOval(x - (SObj - 1) / 2, y - (SObj - 1) / 2, SObj, SObj); // solid circle set
        g.setColor(Color.black); // set outline of circle to black
        g.drawOval(x - (SObj - 1) / 2, y - (SObj - 1) / 2, SObj, SObj); // outline of circle set

        g.drawRect(dbox.x, dbox.y, dbox.width, dbox.height); // draws dragbox
        for (int i = 0; i < Walls.size(); i++) { // loops through vector of rectangles
            Rectangle temp = Walls.elementAt(i); // get the rectangle
            g.fillRect(temp.x, temp.y, temp.width, temp.height); // draws the rectangle
        }
        cg.drawImage(buffer, 0, 0, null); // switches the canvas with the offscreen image
    }

    public void update(Graphics g) {
        paint(g);
    }
}



