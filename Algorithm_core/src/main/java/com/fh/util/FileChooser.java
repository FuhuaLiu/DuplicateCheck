package com.fh.util;

import javax.swing.*;

public class FileChooser {
    private JFileChooser fileChooser;
    private String filePath;

    public FileChooser() {
        this.fileChooser = new JFileChooser();
    }

    public String getFilePath() {
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            filePath = fileChooser.getSelectedFile().getPath();
            return filePath;
        } else {
            System.exit(0);
        }

        return "error";
    }
}
