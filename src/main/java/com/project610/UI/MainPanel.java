package com.project610.UI;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.stream.Stream;

import static com.project610.Utils.prefSize;
import static com.project610.Utils.vbox;

public class MainPanel extends JPanel {
    JFrame parent;
    ArrayList<HornPanel> hornList;
    JPanel listPanel;

    public MainPanel (String[] args, JFrame parent) {
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
            // New stuff
        });

        JMenuItem openItem = new JMenuItem("Open Package");
        fileMenu.add(openItem);
        openItem.addActionListener(e -> {
            // Open stuff
        });

        JMenuItem saveItem = new JMenuItem("Save Package");
        fileMenu.add(saveItem);
        saveItem.addActionListener(e -> {
            try {
                Stream<Path> files;
                String luaFolder = "temp"+File.separator+"lua";
                String hornsFolder = "temp"+File.separator+"horns";

                Files.createDirectories(Paths.get("temp"));
                files = Files.walk(Paths.get("temp"));
                files.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                files.close();

                Files.createDirectories(Paths.get(luaFolder));
                Files.createDirectories(Paths.get(hornsFolder));

                Path luaPath = Paths.get(luaFolder + File.separator + "Horns.lua");

                StringBuilder luaString = new StringBuilder()
                        .append("HORNMOD_AddHorns(\n");

                for (int i = 0; i < hornList.size(); i++) {
                    HornPanel panel = hornList.get(i);
                    try {
                        // Try to copy horn
                        String path = panel.file.getAbsolutePath();
                        Files.copy(Paths.get(path), Paths.get(hornsFolder + File.separator + "DS" + panel.name.toUpperCase(Locale.ROOT) + path.substring(path.lastIndexOf("."))));

                        // Add LUA for horn
                        luaString.append(i == 0 ? "  " : " ,")
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

                // ZIP it up
                // Ninja'd from https://itsallbinary.com/apache-commons-compress-simplest-zip-zip-with-directory-compression-level-unzip/
                try (ZipArchiveOutputStream archive = new ZipArchiveOutputStream(new FileOutputStream("hornpack.zip"))) {

                    File folderToZip = new File("temp");

                    // Walk through files, folders & sub-folders.
                    Files.walk(folderToZip.toPath()).forEach(p -> {
                        File file = p.toFile();

                        // Directory is not streamed, but its files are streamed into zip file with
                        // folder in it's path
                        if (!file.isDirectory()) {
                            System.out.println("Zipping file - " + file);
                            ZipArchiveEntry entry = new ZipArchiveEntry(file, file.toString().substring(5)); // Chop the 'temp/' off the start
                            try (FileInputStream fis = new FileInputStream(file)) {
                                archive.putArchiveEntry(entry);
                                IOUtils.copy(fis, archive);
                                archive.closeArchiveEntry();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    // Complete archive entry addition.
                    archive.finish();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                System.err.println("Failed to save, big time");
                ex.printStackTrace();
            }
        });

        menuBar.add(Box.createHorizontalGlue());

        this.setDropTarget(new DropTarget() {
            public void drop(DropTargetDropEvent e) {
                e.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    List<File> files = (List<File>)e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File f : files) {
                        HornPanel panel = new HornPanel(f);
                        hornList.add(panel);
                        listPanel.add(prefSize(panel,20, 50));
                    }
                    listPanel.revalidate();
                } catch (Exception ex) {
                    System.err.println("Oh no");
                    ex.printStackTrace();
                }
            }
        });

        listPanel = vbox();
        JScrollPane scrollPane = new JScrollPane(listPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(prefSize(scrollPane, 300, 300));

    }

    public void init() {
        hornList = new ArrayList<>();
    }
}
