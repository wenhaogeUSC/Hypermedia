import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Component.LEFT_ALIGNMENT;

public class AuthoringTool {
    public static int primary_frame_num = 0;
    public static int secondary_frame_num = 0;
    final int width = 352;
    final int height = 288;
    final String rgbFileExtension = new String(".rgb");
    Video primaryVideo = new Video();
    Video secondaryVideo = new Video();
    public static Map<Integer, ArrayList<Rect>> primaryVideoLinkmapper = new HashMap<>();
    public static Map<Integer, ArrayList<Rect>> secondaryVideoLinkmapper = new HashMap<>();
    public static Map<JTextField, int[]> linkstoragemap = new HashMap<>();
    //this hashmap stores how many links exist on each frame_num, this is used to track the ordering of adding link to a specific frame
    public static Map<Integer, Integer> frame_rectnum = new HashMap<>();

    public static int cur_fram_num = 0;
    public static int link_order_num = 0;

    // from csci576 hw1 start code
    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try
        {
            int frameLength = width*height*3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    img.setRGB(x,y,pix);
                    ind++;
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void showImg(BufferedImage img, JLabel lbIm, JPanel panel, JSlider slider, Video video) {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();

        String videoName = video.getVideoName();
        String videoPath = video.getVideoPath();
        int frameNum = video.getFrameNum();

        String frameNumString = null;
        if (frameNum >= 1 && frameNum < 10) {
            frameNumString = "000" + String.valueOf(frameNum);
        } else if (frameNum >= 10 && frameNum < 100) {
            frameNumString = "00" + String.valueOf(frameNum);
        } else if (frameNum >= 100 && frameNum < 1000) {
            frameNumString = "0" + String.valueOf(frameNum);
        } else if (frameNum >= 1000 && frameNum < 10000) {
            frameNumString = String.valueOf(frameNum);
        }
        String framePath = videoPath + "/" + videoName + frameNumString + rgbFileExtension;

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readImageRGB(width, height, framePath, img);

        ImageIcon imageIcon = new ImageIcon(img);
        lbIm = new JLabel(imageIcon);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;

        panel.add(lbIm, c);
    }
    private void showFrame() {
        final JFrame frame = new JFrame("Hyper-Linking Video Authoring Tool");
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel rootPanel = new JPanel();

        JPanel topPanel, list, middlePanel, middlePanelLeft, middlePanelRight;
        MouseMotionEvents middleVideoPanelLeft, middleVideoPanelRight;
        JPanel middleSliderPanelLeft, middleSliderPanelRight;
        JButton primaryVideoButton, secondaryVideoButton, createLinkButton, connectButton, saveButton;

        //top panel including all buttons
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

        primaryVideoButton = new JButton("Load Primary Video");
        secondaryVideoButton = new JButton("Load Secondary Video");
        createLinkButton = new JButton("Create new hyperlink");
        connectButton = new JButton("Connect Video");
        saveButton = new JButton("Save File");

        list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));  // vertically append
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(new String("HyperLink List"));
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,1)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        topPanel.add(primaryVideoButton);
        topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        topPanel.add(secondaryVideoButton);
        topPanel.add(createLinkButton);
        topPanel.add(listPane);
        topPanel.add(connectButton);
        topPanel.add(saveButton);
        topPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        //main panel including two input sliders' information
        middlePanelLeft = new JPanel();
        middlePanelLeft.setLayout(new BoxLayout(middlePanelLeft, BoxLayout.Y_AXIS));
        middlePanelLeft.setPreferredSize(new Dimension(420, 350));
        GridBagLayout gLayout = new GridBagLayout();
        middleVideoPanelLeft = new MouseMotionEvents(primaryVideo, gLayout);
        middleSliderPanelLeft = new JPanel();
        middlePanelLeft.add(middleSliderPanelLeft);
        middlePanelLeft.add(middleVideoPanelLeft);


        middlePanelRight = new JPanel();
        middlePanelRight.setLayout(new BoxLayout(middlePanelRight, BoxLayout.Y_AXIS));
        middlePanelRight.setPreferredSize(new Dimension(420, 350));
        middleSliderPanelRight = new JPanel();
        middleVideoPanelRight = new MouseMotionEvents(secondaryVideo, gLayout);
        middlePanelRight.add(middleSliderPanelRight);
        middlePanelRight.add(middleVideoPanelRight);

        middlePanel = new JPanel(new GridLayout(1, 2));
        middlePanel.add(middlePanelLeft);
        middlePanel.add(middlePanelRight);


        BufferedImage frameOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage frameTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        JLabel lbIm1 = new JLabel();
        JLabel lbIm2 = new JLabel();
        JLabel frameOneLabel = new JLabel();
        JLabel frameTwoLabel = new JLabel();

        // 创建一个滑块，最小值、最大值、初始值 分别为 0、20、10
        final JSlider slider1 = new JSlider(JSlider.HORIZONTAL,1, 30, 1);
        final JSlider slider2 = new JSlider(JSlider.HORIZONTAL,1, 30, 1);

        slider1.setPaintTicks(true);
        slider1.setPaintLabels(true);

        slider2.setPaintTicks(true);
        slider2.setPaintLabels(true);

        // 添加刻度改变监听器
        slider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                middleVideoPanelLeft.removeAll();
                middleVideoPanelLeft.revalidate();
                middleVideoPanelLeft.repaint();
                primary_frame_num = slider1.getValue();
                String primaryFrameNum = "Frame " +  slider1.getValue();
                frameOneLabel.setText(primaryFrameNum);
                slider1.setValue(slider1.getValue());

                primaryVideo.setFrameNum(slider1.getValue());
                showImg(frameOne, lbIm1, middleVideoPanelLeft, slider1, primaryVideo);
            }
        });

        slider2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                middleVideoPanelRight.removeAll();
                middleVideoPanelRight.revalidate();
                middleVideoPanelRight.repaint();

                String secondaryFrameNum = "Frame " +  slider2.getValue();
                frameTwoLabel.setText(secondaryFrameNum);
                slider2.setValue(slider2.getValue());

                secondaryVideo.setFrameNum(slider2.getValue());
                showImg(frameTwo, lbIm2, middleVideoPanelRight, slider2, secondaryVideo);

            }
        });

        primaryVideoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == primaryVideoButton)
                {
                    middleVideoPanelLeft.removeAll();
                    middleVideoPanelLeft.revalidate();
                    middleVideoPanelLeft.repaint();
                    middleSliderPanelLeft.removeAll();
                    middleSliderPanelLeft.revalidate();
                    middleSliderPanelLeft.repaint();
                    list.removeAll();
                    list.revalidate();
                    list.repaint();


                    primaryVideoLinkmapper = new HashMap<>();
                    linkstoragemap = new HashMap<>();

                    JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.home"))); //Downloads Directory as default
                    int result = chooser.showSaveDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = chooser.getSelectedFile();
                        System.out.println("Selected primary video file path: " + selectedFile.getAbsolutePath());
                        String videoName = parseVideoName(selectedFile.getName());
                        int videoFrameNum = parseVideoFrameNum(selectedFile.getName());
                        primaryVideo = new Video(videoName, selectedFile.getParent(), videoFrameNum);
                        primary_frame_num = 1;

                        try {
                            FileInputStream fileIn = new FileInputStream(primaryVideo.getVideoPath() + "/primaryVideoLinkmapper.ser");
                            ObjectInputStream in = new ObjectInputStream(fileIn);
                            primaryVideoLinkmapper = (Map<Integer, ArrayList<Rect>>) in.readObject();
                            in.close();
                            fileIn.close();
                        } catch (IOException i) {
                            primaryVideoLinkmapper = new HashMap<>();
                            i.printStackTrace();

                        } catch (ClassNotFoundException c) {
                            System.out.println("primaryVideoLinkmapper class not found");
                            primaryVideoLinkmapper = new HashMap<>();
                            c.printStackTrace();
                        }

                        try {
                            FileInputStream fileIn = new FileInputStream(primaryVideo.getVideoPath() + "/linkstoragemap.ser");
                            ObjectInputStream in = new ObjectInputStream(fileIn);
                            linkstoragemap = (Map<JTextField, int[]>) in.readObject();
                            in.close();
                            fileIn.close();
                            for (JTextField newLink : linkstoragemap.keySet()) {
                                list.add(newLink);
                                list.revalidate();
                                list.repaint();

                                // TODO: not sure if need this code block
                                if(frame_rectnum.get(primary_frame_num) == null){
                                    frame_rectnum.put(primary_frame_num, 1);
                                }else{
                                    int a = frame_rectnum.get(primary_frame_num);
                                    frame_rectnum.put(primary_frame_num, a+1);
                                }

                                newLink.addMouseListener(new MouseListener() {

                                    @Override
                                    public void mouseReleased(MouseEvent e) {// 鼠标松开
                                    }

                                    @Override
                                    public void mousePressed(MouseEvent e) {// 鼠标按下
                                    }

                                    @Override
                                    public void mouseExited(MouseEvent e) {// 鼠标退出组件
                                    }

                                    @Override
                                    public void mouseEntered(MouseEvent e) {// 鼠标进入组件
                                    }

                                    @Override
                                    public void mouseClicked(MouseEvent e) {// 鼠标单击事件
                                        cur_fram_num = linkstoragemap.get(newLink)[0];
                                        link_order_num = linkstoragemap.get(newLink)[1];
                                        slider1.setValue(linkstoragemap.get(newLink)[0]);
                                        middleVideoPanelLeft.revalidate();
                                        middleVideoPanelLeft.repaint();

                                    }
                                });
                            }

                        } catch (IOException i) {
                            linkstoragemap = new HashMap<>();
                            i.printStackTrace();

                        } catch (ClassNotFoundException c) {
                            System.out.println("linkstoragemap class not found");
                            linkstoragemap = new HashMap<>();
                            c.printStackTrace();
                        }


                        middleSliderPanelLeft.add(frameOneLabel);
                        frameOneLabel.setText("Frame " + videoFrameNum);

                        middleSliderPanelLeft.add(slider1);
                        slider1.setValue(videoFrameNum);

                        showImg(frameOne, lbIm1, middleVideoPanelLeft, slider1, primaryVideo);
                    } else if (result == JFileChooser.CANCEL_OPTION) {
                        System.out.println("No file selected");
                    }

                }
            }
        });

        secondaryVideoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == secondaryVideoButton)
                {
                    middleVideoPanelRight.removeAll();
                    middleVideoPanelRight.revalidate();
                    middleVideoPanelRight.repaint();
                    middleSliderPanelRight.removeAll();
                    middleSliderPanelRight.revalidate();
                    middleSliderPanelRight.repaint();

                    JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.home"))); //Downloads Directory as default
                    int result = chooser.showSaveDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = chooser.getSelectedFile();
                        System.out.println("Selected secondary video file path: " + selectedFile.getAbsolutePath());
                        String videoName = parseVideoName(selectedFile.getName());
                        int videoFrameNum = parseVideoFrameNum(selectedFile.getName());
                        secondaryVideo = new Video(videoName, selectedFile.getParent(), videoFrameNum);
                        secondaryVideoLinkmapper = new HashMap<>();
                        middleSliderPanelRight.add(frameTwoLabel);
                        frameTwoLabel.setText("Frame " + videoFrameNum);
                        middleSliderPanelRight.add(slider2);
                        slider2.setValue(videoFrameNum);
                        secondary_frame_num = 1;
                        showImg(frameTwo, lbIm2, middleVideoPanelRight, slider2, secondaryVideo);
                    } else if (result == JFileChooser.CANCEL_OPTION) {
                        System.out.println("No file selected");
                    }

                }
            }
        });

        createLinkButton.addActionListener((ActionEvent e) -> {
            String linkNameInput = JOptionPane.showInputDialog("Enter a link name");
                if(linkNameInput == null){

                }else if (linkNameInput.length()>0) {

                    JTextField newLink = new JTextField(String.valueOf(linkNameInput));

                    if(frame_rectnum.get(primary_frame_num) == null){
                        frame_rectnum.put(primary_frame_num, 1);
                    }else{
                        int a = frame_rectnum.get(primary_frame_num);
                        frame_rectnum.put(primary_frame_num, a+1);
                    }
                    linkstoragemap.put(newLink, new int[]{primary_frame_num, (int)frame_rectnum.get(primary_frame_num)});
                    newLink.addMouseListener(new MouseListener() {

                        @Override
                        public void mouseReleased(MouseEvent e) {// 鼠标松开
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {// 鼠标按下
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {// 鼠标退出组件
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {// 鼠标进入组件
                        }

                        @Override
                        public void mouseClicked(MouseEvent e) {// 鼠标单击事件
                            cur_fram_num = linkstoragemap.get(newLink)[0];
                            link_order_num = linkstoragemap.get(newLink)[1];
                            slider1.setValue(linkstoragemap.get(newLink)[0]);
                            middleVideoPanelLeft.revalidate();
                            middleVideoPanelLeft.repaint();

                        }
                    });

                    list.add(newLink);
                    list.revalidate();
                    list.repaint();
                    System.out.println("New Link created: "  + String.valueOf(linkNameInput));
                }


        });
        //making the button's text editable

        connectButton.addActionListener((ActionEvent e) -> {
            if (secondaryVideo == null) {
                // TODO: if we didn't upload secondary video, we should do nothing
                return;
            }
            if(MouseMotionEvents.targetRectangle != null &&
                    primaryVideoLinkmapper.containsKey(primaryVideo.getFrameNum())){
                Rect targetRect = MouseMotionEvents.targetRectangle;
                targetRect.setSecondaryFrameNum(secondaryVideo.getFrameNum());
                targetRect.setSecondaryVideoName(secondaryVideo.getVideoName());
                System.out.println("targetRect: " + targetRect.cor1);
                System.out.println("targetRect: " + targetRect.cor2);
                System.out.println("targetRect: " + targetRect.getSecondaryFrameNum());
                System.out.println("targetRect: " + targetRect.getSecondaryVideoName());
            }
            MouseMotionEvents.targetRectangle = null;
        });

        saveButton.addActionListener((ActionEvent e) -> {
            try {
                FileOutputStream fileOut = new FileOutputStream(primaryVideo.getVideoPath() + "/primaryVideoLinkmapper.ser");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(primaryVideoLinkmapper);
                out.close();
                fileOut.close();
                System.out.println("Serialized data is saved in " + primaryVideo.getVideoPath() + "/primaryVideoLinkmapper.ser");
            } catch (IOException i) {
                i.printStackTrace();
            }
            try {
                FileOutputStream fileOut = new FileOutputStream(primaryVideo.getVideoPath() + "/linkstoragemap.ser");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(linkstoragemap);
                out.close();
                fileOut.close();
                System.out.println("Serialized data is saved in " + primaryVideo.getVideoPath() + "/linkstoragemap.ser");
            } catch (IOException i) {
                i.printStackTrace();
            }
        });



        frame.setContentPane(rootPanel);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(middlePanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private String parseVideoName(String fileName) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < fileName.length() && Character.isLetter(fileName.charAt(i))) {
            sb.append(fileName.charAt(i++));
        }
        return sb.toString();
    }

    private int parseVideoFrameNum(String fileName) {
        int videoFrameNum = 0;
        int i = 0;
        while (i < fileName.length()) {
            char cur = fileName.charAt(i);
            if (Character.isLetter(cur) || Character.isDigit(cur) && cur == '0') {
                i++;
            } else {
                break;
            }
        }
        while (i < fileName.length() && fileName.charAt(i) != '.') {
            videoFrameNum *= 10;
            videoFrameNum += fileName.charAt(i) - '0';
            i++;
        }
        return videoFrameNum;
    }

    public static class Video {
        private String videoName;
        private String videoPath;
        private int frameNum;

        public Video() {

        }
        public Video(String videoName, String videoPath, int frameNum) {
            this.videoName = videoName;
            this.frameNum = frameNum;
            this.videoPath = videoPath;
        }

        public String getVideoName() {
            return videoName;
        }
        public void setVideoName(String videoName) {
            this.videoName = videoName;
        }

        public String getVideoPath() {
            return videoPath;
        }

        public void setVideoPath(String videoPath) {
            this.videoPath = videoPath;
        }

        public int getFrameNum() {
            return frameNum;
        }

        public void setFrameNum(int frameNum) {
            this.frameNum = frameNum;
        }
    }

    public static void main(String[] args) {
        AuthoringTool authoringTool = new AuthoringTool();
        authoringTool.showFrame();

    }
}
