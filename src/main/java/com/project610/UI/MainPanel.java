package com.project610.UI;

import com.project610.Utils.ZipUtility;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.project610.Utils.Utils.*;

public class MainPanel extends JPanel {
    JFrame parent;
    ArrayList<HornPanel> hornList;
    JPanel listPanel;
    String tempDir = "temp" + File.separator;
    public JTextField filenameField;
    public JTextField versionField;
    File originalFile;
    JCheckBox saveAtSourceBox;


    public MainPanel(String[] args, JFrame parent) {
        this.parent = parent;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.LINE_AXIS));
        parent.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem newItem = new JMenuItem("New Package");
        fileMenu.add(newItem);
        newItem.addActionListener(e -> {
            originalFile = null;
            saveAtSourceBox.setEnabled(false);
            filenameField.setText("");
            versionField.setText("");
            hornList.clear();
            listPanel.removeAll();
            revalidate();
        });

        JMenuItem openItem = new JMenuItem("Open Package");
        fileMenu.add(openItem);
        openItem.addActionListener(e -> {
            // UNZIP
            String filename;
            String unzipDir = tempDir + "unpacked" + File.separator;
            try {
                FileDialog fd = new FileDialog(parent, "Select a horn package (pk3)");
                fd.setLocation(this.getWindowCenter());
                fd.setVisible(true);
                if (fd.getFile() == null) return;
                filename = fd.getDirectory() + File.separator + fd.getFile();
            } catch (Exception ex) {
                System.err.println("Failed to load package");
                ex.printStackTrace();
                return;
            }

            // UNZIP it down
            LinkedHashMap<String, File> horns = new LinkedHashMap<>();
            LinkedHashMap<String, File> extraFiles = new LinkedHashMap<>();
            LinkedHashMap<String, String> infos = new LinkedHashMap<>();
            LinkedHashMap<String, String> extagses = new LinkedHashMap<>();

            Process proc = null;
            BufferedReader br = null;
            try {
                Files.createDirectories(Paths.get(unzipDir));

                String osName = System.getProperty("os.name");
                if (osName.toLowerCase().startsWith("windows")) {
                    proc = Runtime.getRuntime().exec("bin" + File.separator + "7z.exe -bb1 -y x \"" + filename + "\" -o\"" + unzipDir + "\"");
                    System.out.println("Unzipping with 7z.exe");
                    proc.waitFor();
                } else {
                    // TODO ;)
                }

                br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    System.out.println(line);

                    // Trim the "- " if it exists, skip the line if it doesn't
                    if (!line.startsWith("- ")) continue;
                    line = line.substring(2);

                    if (Files.isDirectory(Paths.get(unzipDir + line))) continue;

                    File file = new File(unzipDir + line);
                    System.out.println("Unzipping - " + file);
                    String dir = file.toPath().toString().substring(0, file.toPath().toString().lastIndexOf("\\"));


                    if (dir.toLowerCase().contains("horn")) {
                        // Trim the DS and extension
                        String name = file.getName().substring(2, (file.getName().contains(".") ? file.getName().lastIndexOf(".") : file.getName().length())).toLowerCase();
                        horns.put(name, file);
                        extraFiles.put(name, file);
                    } else if (dir.toLowerCase().contains("lua")) {
                        String lua = new String(Files.readAllBytes(file.toPath()));
                        String temp;
                        while (lua.contains("{")) {
                            lua = lua.substring(lua.indexOf("{"));
                            temp = lua.substring(0, lua.indexOf("}"));

                            String name = temp.substring(temp.indexOf("name")).toLowerCase();
                            name = name.substring(name.indexOf("sfx_") + 4);
                            name = name.substring(0, name.indexOf('"'));
                            System.out.println("name is: " + name);

                            String info = temp.substring(temp.toLowerCase().indexOf("info"));
                            info = info.substring(info.indexOf('"') + 1);
                            info = info.substring(0, info.indexOf('"'));
                            System.out.println("info is: " + info);
                            infos.put(name, info);

                            if (temp.toLowerCase().contains("extags")) {
                                String extags = temp.substring(temp.toLowerCase().indexOf("extags"));
                                extags = extags.substring(extags.indexOf('"') + 1);
                                extags = extags.substring(0, extags.indexOf('"'));
                                extagses.put(name, extags);
                                System.out.println("extags is: " + extags);
                            } else {
                                extagses.put(name, "");
                            }

                            System.out.println("---");
                            extraFiles.put(name, file);


                            lua = lua.substring(lua.indexOf("}"));
                        }
                    } else {
                        String name = file.getName();
                        extraFiles.put(name, file);
                    }
                }

                for (String name : infos.keySet()) {
                    newHorn(horns.get(name), name, infos.get(name), "" + extagses.get(name), false);
                    extraFiles.remove(name);
                }

                System.out.println("\nChecking for extra files... (Not doing anything about it though~)");
                for (String key : extraFiles.keySet()) {
                    System.out.println("Extra: " + key);
                }
                listPanel.revalidate();

                // Loaded everything fine, update the file info panel
                String justName = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.lastIndexOf(".")), version = "";
                if (filename.contains("-v")) {
                    version = justName.substring(justName.indexOf("-v") + 2);
                    justName = justName.substring(0, justName.indexOf("-v"));
                }
                filenameField.setText(justName);
                versionField.setText(version);
                originalFile = new File(filename);
                saveAtSourceBox.setEnabled(true);

            } catch (Exception ex) {
                System.err.println("Failed to unzip");
                ex.printStackTrace();
                return;
            }


        });

        JMenuItem saveItem = new JMenuItem("Save Package");
        fileMenu.add(saveItem);
        saveItem.addActionListener(e -> savePackage());


        menuBar.add(Box.createHorizontalGlue());

        this.setDropTarget(new DropTarget() {
            public void drop(DropTargetDropEvent e) {
                e.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    List<File> files = (List<File>) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File f : files) {
                        newHorn(f);
                    }
                    listPanel.revalidate();
                } catch (Exception ex) {
                    System.err.println("Oh no");
                    ex.printStackTrace();
                }
            }
        });

        add(Box.createRigidArea(new Dimension(1, 3)));
        JPanel filePanel = hbox();
        add(maxSize(filePanel, Integer.MAX_VALUE, 20));
        filePanel.add(new JLabel("  Filename: "));

        filenameField = new JTextField(20);
        filePanel.add(filenameField);

        filePanel.add(new JLabel("      Version: "));
        versionField = new JTextField(4);
        filePanel.add(versionField);

        filePanel.add(Box.createRigidArea(new Dimension(30, 1)));

        saveAtSourceBox = new JCheckBox("Save in source directory");
        saveAtSourceBox.setEnabled(false);
        filePanel.add(saveAtSourceBox);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> savePackage());
        filePanel.add(saveButton);

        filePanel.add(Box.createHorizontalGlue());

        add(Box.createRigidArea(new Dimension(1, 3)));

        listPanel = vbox();
        JScrollPane scrollPane = new JScrollPane(listPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(17);
        add(prefSize(scrollPane, 300, 300));

    }

    private void newHorn(File f) {
        newHorn(f, "", "", "", true);
    }

    private void newHorn(File f, String name, String info, String extags, boolean actuallyNew) {
        if (!actuallyNew && (null == info || info.isEmpty())) {
            System.out.println("Nope, info was blank: " + name);
            return;
        }
        try {
            if (!name.isEmpty() || !info.isEmpty()) {
                for (HornPanel p : hornList) {
                    if (name.equals(p.name) || info.equals(p.info)) {
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Failed to load file name: " + name + ", info: " + info);
        }
        HornPanel panel = new HornPanel(this, f, name, info, extags);
        hornList.add(panel);
        listPanel.add(prefSize(panel, 20, 50));
    }

    public void deleteHorn(HornPanel panel) {
        listPanel.remove(panel);
        hornList.remove(panel);
        if (hornList.size() == 0) {
            repaint();
        }
        revalidate();
    }

    public Point getWindowCenter() {
        return new Point(parent.getLocation().x + parent.getWidth() / 2, parent.getLocation().y + parent.getHeight() / 2);
    }

    public void showWarning(String message) {
        JDialog warning = new JDialog(parent, "Warning", true);
        warning.setLayout(new BorderLayout());
        warning.setLocation(this.getWindowCenter());
        warning.add(new JLabel("<html><div style='width: 200px; padding: 20px 20px 20px 5px;'>" + message + "</div>"), BorderLayout.CENTER);
        JButton closeButton = new JButton("OK");
        closeButton.addActionListener(e -> warning.dispose());
        warning.add(closeButton, BorderLayout.LINE_END);
        warning.pack();
        warning.setVisible(true);
    }

    public void savePackage() {
        try {
            if (filenameField.getText().trim().isEmpty()) {
                showWarning("Can't save without a filename");
                return;
            }
            if (hornList.size() == 0) {
                showWarning("Nothing to save");
                return;
            }

            String filename = filenameField.getText().trim();
            String version = versionField.getText().trim();
            String rootPath = "";
            if (saveAtSourceBox.isEnabled() && saveAtSourceBox.isSelected()) {
                rootPath = originalFile.getAbsolutePath();
                rootPath = rootPath.substring(0, rootPath.lastIndexOf(File.separator) + 1);
            }

            Stream<Path> files;
            String zipDir = tempDir + "zip" + File.separator;
            String luaDir = zipDir + "lua" + File.separator;
            String hornsDir = zipDir + "horns" + File.separator;

            Files.createDirectories(Paths.get(zipDir));
            files = Files.walk(Paths.get(zipDir));
            files.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            files.close();
            Files.createDirectories(Paths.get(zipDir));

            Files.createDirectories(Paths.get(luaDir));
            Files.createDirectories(Paths.get(hornsDir));

            Path luaPath = Paths.get(luaDir + "Horns.lua");

            StringBuilder luaString = new StringBuilder()
                    .append("HORNMOD_AddHorns(\n");

            for (int i = 0; i < hornList.size(); i++) {
                if (i > 0 && i % 200 == 0) {
                    luaString
                            .append(")\n\n")
                            .append("HORNMOD_AddHorns(\n");
                }
                HornPanel panel = hornList.get(i);
                try {
                    // Try to copy horn
                    String path = panel.file.getAbsolutePath();
                    Files.copy(Paths.get(path), Paths.get(hornsDir + "DS" + panel.name.toUpperCase(Locale.ROOT) + (path.contains(".") ? path.substring(path.lastIndexOf(".")) : "")));

                    // Add LUA for horn
                    luaString.append(i % 200 == 0 ? "  " : " ,")
                            .append("{name=\"sfx_").append(panel.name.toLowerCase(Locale.ROOT))
                            .append("\", info=\"").append(panel.info)
                            .append("\", extags=\"").append(panel.extags)
                            .append("\"}\n");
                } catch (FileAlreadyExistsException ex) {
                    System.err.println("Duplicate horn name: '" + panel.name + "'");
                }
            }

            luaString.append(")\n");

            Files.write(luaPath, luaString.toString().getBytes(StandardCharsets.UTF_8));

            String outFileZip = rootPath + filename + (version.isEmpty() ? "" : "-v" + version) + ".zip";
            String outFilePk3 = rootPath + filename + (version.isEmpty() ? "" : "-v" + version) + ".pk3";

            // Remove any old one
            Files.deleteIfExists(Paths.get(outFileZip));
            Files.deleteIfExists(Paths.get(outFilePk3));

            // ZIP it up
            String osName = System.getProperty("os.name");
            Process proc;
            if (osName.toLowerCase().startsWith("windows")) {
                proc = Runtime.getRuntime().exec("bin" + File.separator + "7z.exe a \"" + outFileZip + "\" ./temp/zip/*");
                System.out.println("Zipping with 7z.exe");

                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } else {
                // TODO ;)
            }

            System.out.println("Renaming zip to pk3");
            new File(outFileZip).renameTo(new File(outFilePk3));

        } catch (Exception ex) {
            System.err.println("Failed to save, big time");
            ex.printStackTrace();
        }
    }

    public void init() {
        hornList = new ArrayList<>();
    }
}