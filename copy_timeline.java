package test;

import javax.swing.*;
import java.awt.*;
import java.io.*;


public class copy_timeline {
	interface CallBack {
        void showPercentage(long percentage, long totalBytesRead);
    }

    static class CallBackImpl implements CallBack {
        private JProgressBar progressBar;

        public CallBackImpl(JProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        public void showPercentage(long percentage, long totalBytesRead) {
            progressBar.setValue((int) percentage);
        }
    }

    public static void copyFileWithProgress(File sourceFile, File destFile, CallBack callBack) throws IOException {
        try (InputStream input = new FileInputStream(sourceFile);
             OutputStream output = new FileOutputStream(destFile)) {

            final int sizeOfCopyBlock = 1024;
            long totalBytesRead = 0;
            long sizeOfSourceFile = sourceFile.length();
            long pLast = 0;
            long pCurrent = 0;
            byte[] buffer = new byte[sizeOfCopyBlock];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) > 0) {
                output.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                pCurrent = (totalBytesRead * 100) / sizeOfSourceFile;
                if (pLast != pCurrent) {
                    callBack.showPercentage(pCurrent, totalBytesRead);
                    pLast = pCurrent;
                }
            }
        }
    }

    public static void main(String[] args) {
        // 創建一個文件選擇器
        JFileChooser fileChooser = new JFileChooser();

        // 創建一個文件選擇器視窗
        JFrame fileChooserFrame = new JFrame("Choose Source File");
        fileChooserFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fileChooserFrame.setSize(600, 400);
        fileChooserFrame.add(fileChooser);
        fileChooserFrame.setVisible(true);

        // 等待用戶選擇源文件
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            // 獲取用戶選擇的源文件
            File sourceFile = fileChooser.getSelectedFile();

            // 再次使用文件選擇器讓用戶選擇複製到的位置
            result = fileChooser.showSaveDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                // 獲取用戶選擇的目標位置
                File destFile = fileChooser.getSelectedFile();

                // 創建ProgressBar
                JProgressBar progressBar = new JProgressBar();
                progressBar.setStringPainted(true);

                // 創建回調實例並將ProgressBar傳遞給它
                CallBack callBack = new CallBackImpl(progressBar);

                // 創建窗口並將ProgressBar添加到其中
                JFrame frame = new JFrame("File Copy Progress");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(progressBar, BorderLayout.CENTER);
                frame.setSize(300, 100);
                frame.setVisible(true);

                try {
                    copyFileWithProgress(sourceFile, destFile, callBack);
                } catch (IOException e) {
                    System.err.println("文件複製過程中發生錯誤: " + e.getMessage());
                }
            }
        }
    }
}
