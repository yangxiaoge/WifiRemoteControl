package com.yjn.socket.client_gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Client extends JFrame {
    JLabel label;
    boolean isMove = false;
    JLabel tips;

    public Client() throws IOException {
        setLayout(new BorderLayout(0, 0));
        JPanel ipPanel = new JPanel(new BorderLayout(5, 5));
        final JTextField ipField = new JTextField();
        ipField.setText("192.168.1.112");
//        ipField.setVisible(false);
        ipPanel.add(ipField, BorderLayout.CENTER);
        //ipPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel portPanel = new JPanel(new BorderLayout(5, 5));
        final JTextField portField = new JTextField();
        portField.setText("8888");
//        portField.setVisible(false);
        portPanel.add(portField, BorderLayout.CENTER);
        //portPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel btnPanel = new JPanel(new BorderLayout(5, 5));
        JButton btn = new JButton("Connect");
        btnPanel.add(btn, BorderLayout.NORTH);

        tips = new JLabel();
        tips.setBorder(new EmptyBorder(0, 8, 0, 0));
        tips.setText("Image Quality 100%");
        btnPanel.add(tips, BorderLayout.CENTER);


        JSlider jSlider = createSlider();
        btnPanel.add(jSlider, BorderLayout.SOUTH);

        JPanel panelContainer = new JPanel(new BorderLayout());
        panelContainer.add(ipPanel, BorderLayout.NORTH);
        panelContainer.add(portPanel, BorderLayout.CENTER);
        panelContainer.add(btnPanel, BorderLayout.SOUTH);


        JPanel panelContainer2 = new JPanel(new BorderLayout());
        panelContainer2.add(panelContainer, BorderLayout.NORTH);

        label = new JLabel();
        label.setIcon(new ImageIcon("client_gui/nosignal.jpg"));
        label.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.red));

        add(panelContainer2, BorderLayout.NORTH);

        add(label, BorderLayout.CENTER);

        add(createTableBar(), BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(360, 20, 350, 600);

        setTitle("Share Screen");
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    read(ipField.getText(), portField.getText());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        });


        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                int x = mouseEvent.getX();
                int y = mouseEvent.getY();
                try {
                    System.out.println(111111111 + "Clicked DOWN UP");
                    writer.write("down#" + (x * 1.0f / label.getWidth()) + "#" + (y * 1.0f / label.getHeight()));
                    writer.newLine();
                    writer.write("up#" + (x * 1.0f / label.getWidth()) + "#" + (y * 1.0f / label.getHeight()));
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {

                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                super.mouseReleased(mouseEvent);
                try {
                    System.out.println(22222222 + "Released UP");
                    int x = mouseEvent.getX();
                    int y = mouseEvent.getY();
                    writer.write("up#" + (x * 1.0f / label.getWidth()) + "#" + (y * 1.0f / label.getHeight()));
                    writer.newLine();
                    writer.flush();
                    isMove = false;
                } catch (Exception e) {

                }
            }
        });
        label.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                super.mouseDragged(mouseEvent);
                try {
                    System.out.println(333333 + "Dragged DOWN MOVE");
                    int x = mouseEvent.getX();
                    int y = mouseEvent.getY();
                    if (!isMove) {
                        isMove = true;

                        writer.write("down#" + (x * 1.0f / label.getWidth()) + "#" + (y * 1.0f / label.getHeight()));
                    } else {

                        writer.write("move#" + (x * 1.0f / label.getWidth()) + "#" + (y * 1.0f / label.getHeight()));
                    }
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {

                }
            }


        });

        setSize(380, 730);// 设置窗体大小

    }

    private JSlider createSlider() {
        int minimum = 30;
        int maximum = 100;
        JSlider slider = new JSlider(minimum, maximum, maximum);

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                try {
                    int v = ((JSlider) changeEvent.getSource()).getValue();
                    tips.setText("Image Quality " + v + "%");
                    writer.write("degree#" + v);
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        return slider;
    }

    private JPanel createTableBar() {
        JPanel bar = new JPanel(new BorderLayout());
        JPanel bar1 = new JPanel(new FlowLayout());
        JButton menu = new JButton("menu");
        JButton home = new JButton("home");
        JButton back = new JButton("back");
        JButton power = new JButton("power");
        JButton recent = new JButton("recent");
        bar1.add(menu);
        bar1.add(home);
        bar1.add(back);
        bar1.add(power);
        bar1.add(recent);

        bar.add(bar1, BorderLayout.SOUTH);

        JPanel bar2 = new JPanel(new FlowLayout());
        JButton volumeincrease = new JButton("volume+");
        JButton volumedecrease = new JButton("volume-");
        bar2.add(volumeincrease);
        bar2.add(volumedecrease);

        bar.add(bar2, BorderLayout.NORTH);

        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("menu");
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        home.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("home");
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("back");
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        recent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("recent");
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        power.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("power");
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        volumeincrease.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("volumeincrease");
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        volumedecrease.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("volumedecrease");
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return bar;
    }

    BufferedWriter writer;

    private void read(final String ip, final String port) throws IOException {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Socket socket = new Socket(ip, Integer.parseInt(port));
                    BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    byte[] bytes = null;
                    while (true) {
                        long s1 = System.currentTimeMillis();
                        int version = inputStream.read();
                        if (version == -1) {
                            return;
                        }
                        int length = readInt(inputStream);
                        if (bytes == null) {
                            bytes = new byte[length];
                        }
                        if (bytes.length < length) {
                            bytes = new byte[length];
                        }
                        int read = 0;
                        while ((read < length)) {
                            read += inputStream.read(bytes, read, length - read);
                        }
                        InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        long s2 = System.currentTimeMillis();
                        Image image = ImageIO.read(byteArrayInputStream);
                        //System.out.println(((BufferedImage) image).getWidth()+" "+((BufferedImage) image).getHeight());
                        label.setIcon(new ScaleIcon(new ImageIcon(image)));
                        long s3 = System.currentTimeMillis();
                        //System.out.println("s3-s1 " + (s3 - s1) + " s2-s1 " + (s2 - s1));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private int readInt(InputStream inputStream) throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        int b4 = inputStream.read();

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    public static void main(String[] args) throws IOException {
        new Client().setVisible(true);
    }
}

