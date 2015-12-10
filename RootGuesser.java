import javax.swing.*;
import java.awt.*;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.io.*;
import java.awt.event.*;

/**
 * Choose a root and be given numbers, the roots of which you guess. 
 * You'll be given your percent error and and told over or under the root.
 * 
 * @author Owen Levin 
 * @version 1.0
 */
public class RootGuesser extends JFrame implements WindowListener
{
    /*
     * RootListener is a listener class to handle the event of the pressing of any of the Nth root buttons
     * 
     * when activated, an inputDialog asking the user to input their guess at the Nth root 
     * of a randomly generated number between 0 and 1000000 will open.
     * The user must then input a number and will be re-prompted if they enter a non-numeric String.
     * A messageDialog then displays the percent error of the user's guess, and tells them the answer.
     * The score will then increase (or decrease) by N - floor(|percent error|)
     * if the score surpasses the high score, then the high score will also update accordingly
     * 
     * @see input dialog
     * @see message dialog
     * @see score change
     * @see possible high score change
     * 
     */
    private class RootListener implements ActionListener
    {
        private int root;
        private RootListener(int root)
        {
            this.root = root;
        }
        
        public void actionPerformed(ActionEvent event)
        {
            int numberToBeGuessed = 0;
            if(settingIsDecimal)
            {
                numberToBeGuessed = (int)(Math.random()*1000000);
            }
            else
            {
                numberToBeGuessed = (int)(Math.pow((int)(Math.random()*Math.pow(1000000,1.0/root)), root));
            }
            String [] rootString = rootToString(root);
            
            //ask question
            String numText = JOptionPane.showInputDialog("Find the" + rootString[2] + numberToBeGuessed);
            
            //re-ask if the answer was not a number until the user inputs a number.
            while(!isNumeric(numText))
            {
                numText = JOptionPane.showInputDialog("No, silly! the" + rootString[2]
                                         + numberToBeGuessed + " is a positive number! What  do you think that number is?");
            }
            
            //give feedback to the user's answer
            double guess = Double.parseDouble(numText);
            double errorPercent = (100*(Math.pow(guess, root) - numberToBeGuessed)/numberToBeGuessed);
            updateScore(outputFeedback(root, rootString, errorPercent, guess, numberToBeGuessed));
        }
    }
    
    private class ResetListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            highScore = 0;
            writeDataToRGHS();
            updateScore(0);
        }
    }
    
    private class SettingListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            switchSetting();
            writeDataToRGHS();
        }
    }
    
    private JButton integerOrDecimalSwitch;
    private JButton squareRoot;
    private JButton cubedRoot;
    private JButton fourthRoot;
    private JButton sixthRoot;
    private JButton reset;
    
    private JLabel highScoreDisplay;
    private JLabel scoreDisplay;
    private JLabel settingDisplay;
    private JPanel panel;
    
    private int score;
    private int highScore;
    private boolean settingIsDecimal;
    
    private static final int windowWidth = 370;
    private static final int windowHeight = 190;
    private static final double  EPSILON = 1*Math.pow(10,-8);
    
    /*
     * constructs an instance of a RootGuesser frame 
     */
    public RootGuesser()
    {
        super("Root Guesser");
        
        
        score = 0;
        highScore = readHighScore();
        settingIsDecimal = true;
        settingIsDecimal = readSetting();
        
        highScoreDisplay = new JLabel("High Score: " + highScore + "            ");
        scoreDisplay = new JLabel("Current Score: " + score);
        
        String setting = settingName();
        settingDisplay = new JLabel("                 Current Setting: " + setting + "                        ");
        
        addWindowListener(this);
        createSquareRoot();
        createCubeRoot();
        createFourthRoot();
        createSixthRoot();
        createResetButton();
        createSettingButton();
        createPanel(0);
        
        setSize(windowWidth, windowHeight);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args)
    {
        RootGuesser rootGuesser = new RootGuesser();
    }
    
    
    
    
    
    //BUTTON AND PANEL CREATIONS
    
    /*
     * The following create?Root() methods' functions are explained in the RootListener class dscription
     */
    private void createSquareRoot()
    {
        squareRoot = new JButton("Square Root");
        ActionListener listener = new RootListener(2);
        squareRoot.addActionListener(listener);
    }
    
    private void createCubeRoot()
    {
        cubedRoot = new JButton("Cube Root");
        ActionListener listener = new RootListener(3);
        cubedRoot.addActionListener(listener);
    }
    
    private void createFourthRoot()
    {
        fourthRoot = new JButton("Fourth Root");
        ActionListener listener = new RootListener(4);
        fourthRoot.addActionListener(listener);
    }
    
    private void createSixthRoot()
    {
        sixthRoot = new JButton("Sixth Root");
        ActionListener listener = new RootListener(6);
        sixthRoot.addActionListener(listener);
    }
    
    /*
     * Creates a button that resets the high score when pressed
     * 
     * @see high score displayed in RootGuesser instance will be 0
     */
    private void createResetButton()
    {
        reset = new JButton("Reset High Score");
        ActionListener listener = new ResetListener();
        reset.addActionListener(listener);
    }
    
    /*
     * Creates a button that switches to and from decimal or integer calculations
     * 
     * @see high score displayed in RootGuesser instance will be 0
     */
    private void createSettingButton()
    {
        integerOrDecimalSwitch = new JButton("Decimal/Integer");
        ActionListener listener = new SettingListener();
        integerOrDecimalSwitch.addActionListener(listener);
    }
    
    /*
     * creates the panel to display on the RootGuesser
     */
    private void createPanel(int score)
    {
        panel = new JPanel();
        panel.add(highScoreDisplay);
        panel.add(scoreDisplay);
        panel.add(settingDisplay);
        
        panel.add(squareRoot);
        panel.add(cubedRoot);
        panel.add(fourthRoot);
        panel.add(sixthRoot);
        panel.add(reset);
        panel.add(integerOrDecimalSwitch);
        add(panel);
    }
    
    
    
    
    
    //SCORE METHODS and SETTING METHODS:  updateScore, readHighScore, writeDataToRGHS
    
    /*
     * changes score by the value given as input but does not go below 0
     * updates the high score if necessary
     * updates the score displays
     * 
     * @param amount to change score by
     * @see updated scores
     */
    private void updateScore(int scoreChange)
    {
        if(score + scoreChange >= 0)
        {
            score += scoreChange;
        } 
        else 
        {
            score = 0;
        } 
        
        //change high score if needed
        if(score > highScore)
        {
            highScore = score;
            highScoreDisplay.setText("High Score: " + highScore + "            ");
        }
        scoreDisplay.setText("Current Score: " + score);
    }
    
    /*
     * Reads in the high score from a file in the same directory as the RootGuesser.jar 
     * titled RGHS.txt for Root Guesser High Score. If there is no such file or if the 
     * file does not contain numbers, the file is created and high score is set to the 
     * current score.  The file is assumed to contain an int and be within the java 
     * maximum int range
     * 
     * @return highScore (as read from the file RGHS.txt)
     */
    private int readHighScore()
    {
        File dataFile = new File("./RGHS.txt");
        if(dataFile.exists())
        {
            Path file = dataFile.toPath();
            try (InputStream in = Files.newInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) 
            {
                String line = null;
                String dataText = " ";
                String highScoreText = " ";
                if((line = reader.readLine()) != null) 
                {
                    dataText = line;
                }
                highScoreText = dataText.substring(0,dataText.indexOf(" "));
                if(isNumeric(highScoreText))
                {
                    highScore = (int) Integer.parseInt(highScoreText);
                }
                else
                {
                    highScore = score;
                    writeDataToRGHS();
                }
            } 
            catch (IOException x) 
            {
                System.out.println("FAILED TO WRITE FILE");
            }
        }
        else
        {
            highScore = score;
            writeDataToRGHS();
            updateScore(0);
        }
        return highScore;
    }
    
    private boolean readSetting()
    {
        File dataFile = new File("./RGHS.txt");
        if(dataFile.exists())
        {
            Path file = dataFile.toPath();
            try (InputStream in = Files.newInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) 
            {
                String line = null;
                String dataText = " ";
                String setting = " ";
                if((line = reader.readLine()) != null) 
                {
                    dataText = line;
                }
                setting = dataText.substring(dataText.indexOf(" ")+1);
                return setting.equals("true");
            } 
            catch (IOException x) 
            {
                System.out.println("FAILED TO WRITE FILE");
            }
        }
        else
        {
            writeDataToRGHS();
        }
        return settingIsDecimal;
    }
    
    private void writeDataToRGHS()
    {
        File oldFile = new File("./RGHS.txt");
        oldFile.delete();
        String dataText = "" + highScore + " " + settingIsDecimal;
        byte data[] = dataText.getBytes();
        Path p = Paths.get("./RGHS.txt");
    
        try (OutputStream out = new BufferedOutputStream(
             Files.newOutputStream(p, CREATE, APPEND))) 
        {
          out.write(data, 0, data.length);
        } 
        catch (IOException x) 
        {
          System.out.println("FAILED TO WRITE FILE");
        }
    }
    
    private void switchSetting()
    {
        settingIsDecimal = !settingIsDecimal;
        writeDataToRGHS();
        String setting = settingName();
        settingDisplay.setText("                 Current Setting: " + setting +"                        ");
    }
    
    private String settingName()
    {
         if(settingIsDecimal)
        {
            return "Decimal";
        }
        else
        {
            return "Integer";
        }
    }
    
    //HELPER METHOD(S):  rootToString, outputFeedback, isNumeric
    
    /*
     * based on the root returns a String array used in RootListeners' actionPerformed() and in outputFeedback()
     *
     *@return array of text snippets used in forming strings in actionPerformed() and in outputFeedback()
     */
    private String [] rootToString(int root)
    {
        String[] x = new String[3];
        switch (root) 
        {
            case 2:  x = new String[]{" squared is ","\n The actual square root was "," square root of "};
                     break;
            case 3:  x = new String[]{" cubed is ","\n The actual cubed root was "," cubed root of "};
                     break;
            case 4:  x = new String[]{" to the fourth power is ","\n The actual fourth root was "," fourth root of "};
                     break;
            case 6:  x = new String[]{" to the sixth power is ","\n The actual sixth root was "," sixth root of "};
                     break;
            default: x = new String[]{"","",""};
        }
        return x;
    }
    
    /*
     * outputs feedback on a user's guess of an nth root with percentage error,
     * what their guess to nth power is, and the actual answer
     * 
     * @return amount to update score
     */
    private int outputFeedback(int root, String[] rootSpecifiedWord, double errorPercent,double guess, int numberToBeGuessed)
    {
        String overOrUnder;
        if(errorPercent > 0)
        {
            overOrUnder = "You were over by ";
        } 
        else 
        {
            overOrUnder = "You were under by ";
        }
        double answer = Math.pow(numberToBeGuessed*1.0, 1.0/root);
        double positivePercentageError = (Math.abs(guess - answer)/answer*10000)/100.0;
        if(Math.abs(answer-guess) < EPSILON)
        {
            positivePercentageError = 0;
            answer = guess;
            overOrUnder = "You were off by ";
        }
        String feedback = "";
        
        //create feedback
        feedback += overOrUnder + positivePercentageError + 
                    " percent. \n" + guess + rootSpecifiedWord[0] + 
                    Math.pow(guess,root)+ rootSpecifiedWord[1] + 
                    answer;
        if (positivePercentageError < .1)
        {
            feedback += "\n          Congratulations on that amazing guess!";
        } 
        else if (positivePercentageError < 1)
        {
            feedback += "\n                                     Great job!";
        }
        else if(Math.floor(positivePercentageError) > root)
        {
            feedback += "\n                                     UR A SCRUB";//"\n                Oh well, better luck next time!";
        }
        JOptionPane.showMessageDialog(new JFrame(),feedback,"Percent Error:" + rootSpecifiedWord[2] + numberToBeGuessed,JOptionPane.PLAIN_MESSAGE);
                
        //return change in score
        return root-(int)Math.floor(positivePercentageError);
    }
    
    
    /*
     * @return whether a string contains just a number or not
     * returns false if the string has more than one decimal point
     *            or if the string has a decimal without digits
     *            or if there are no digits
     */
    public static boolean isNumeric(String str)
    {
        boolean hasDigit = false;
        int decimalCount =0;
        for (char c : str.toCharArray())
        {
            if (Character.isDigit(c))
            {
                hasDigit = true;
            }
            else if(c != '.')
            {
                return false;
            }
            else
            {
                decimalCount += 1;
                if(decimalCount > 1)
                {
                    return  false;
                }
            }
        }
        return hasDigit;
    }
    
    
    
    
    
    //WINDOW METHODS
    
    public void windowClosing(WindowEvent e) {
        writeDataToRGHS();
        this.dispose();
    }
    
    public void windowClosed(WindowEvent e) {
    }
    public void windowOpened(WindowEvent e) {
    }
    public void windowIconified(WindowEvent e) {
    }
    public void windowDeiconified(WindowEvent e) {
    }
    public void windowActivated(WindowEvent e) {
    }
    public void windowDeactivated(WindowEvent e) {
    }
    public void windowGainedFocus(WindowEvent e) {
    }
    public void windowLostFocus(WindowEvent e) {
    }
    public void windowStateChanged(WindowEvent e) {
    }
}

