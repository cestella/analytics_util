package com.caseystella.output;

import com.caseystella.summarize.Summary;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CursesVisualize {
  public void display(Map<String, Summary> columnSummaries) throws IOException {
    // Setup terminal and screen layers
    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    Screen screen = new TerminalScreen(terminal);
    screen.startScreen();
    // Create gui and start gui
    MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
    ActionListDialogBuilder builder = new ActionListDialogBuilder().setTitle("Columns")
                                                                   .setDescription("Choose a column");
    for(Map.Entry<String, Summary> kv : columnSummaries.entrySet()) {
      builder = builder.addAction(kv.getKey(), new ColumnDisplayer(gui, kv.getValue(), kv.getKey()));
    }
    builder.build()
            .showDialog(gui);
  }


  public static class ColumnDisplayer implements Runnable {
    Summary columnSummary;
    MultiWindowTextGUI gui;
    String column;
    public ColumnDisplayer(MultiWindowTextGUI gui, Summary columnSummary, String column) {
      this.columnSummary = columnSummary;
      this.gui = gui;
      this.column = column;
    }

    public Table<String> getCountTable() {
      Table<String> table = new Table<>("Type", "Modifier", "Count", "Distinct Count");
      Map<Tuple2<String, String>, Tuple2<String, String>> aggregateMap = new HashMap<>();
      for(Map<String, Object> map : columnSummary.getCountByType()) {
        String typeMod = map.get("type").toString();
        Iterable<String> it = Splitter.on(":").split(typeMod);
        String type = Iterables.getFirst(it, null);
        String modifier = Iterables.getLast(it, null);
        String count = map.get("count").toString();
        aggregateMap.put(new Tuple2<>(type, modifier), new Tuple2<>(count , "0"));
      }
      for(Map<String, Object> map : columnSummary.getCountDistinctByType()) {
        String typeMod = map.get("type").toString();
        Iterable<String> it = Splitter.on(":").split(typeMod);
        String type = Iterables.getFirst(it, null);
        String modifier = Iterables.getLast(it, null);
        String count = map.get("count").toString();
        Tuple2<String, String> key = new Tuple2<>(type, modifier);
        Tuple2<String, String> val = aggregateMap.get(key);
        aggregateMap.put(key, new Tuple2<>(val._1, count));
      }
      for(Map.Entry<Tuple2<String, String>, Tuple2<String, String>> kv : aggregateMap.entrySet()) {
        table.getTableModel().addRow(kv.getKey()._1, kv.getKey()._2, kv.getValue()._1, kv.getValue()._2);
      }
      return table;
    }

    public Map<String, Table<String>> getValueTable() {
      Map<String, Table<String>> ret = new HashMap<>();
      for(Map<String, Object> kv : columnSummary.getNonNumericValueSummary()) {
        String typeMod = kv.get("type").toString();
        Iterable<String> it = Splitter.on(":").split(typeMod);
        String type = Iterables.getFirst(it, null);
        String modifier = Iterables.getLast(it, null);
        Map<String, Double> valueMap = (Map<String, Double>) kv.get("summary");
        Table<String> t = new Table<>("Canonical Value", "Count");
        for(Map.Entry<String, Double> summarykv : valueMap.entrySet()) {
          t.getTableModel().addRow(summarykv.getKey(), summarykv.getValue().longValue() + "");
        }
        ret.put(modifier + " " + type + " Canonical Representation Count", t );
      }
      for(Map<String, Object> kv : columnSummary.getNumericValueSummary()) {
        String typeMod = kv.get("type").toString();
        Iterable<String> it = Splitter.on(":").split(typeMod);
        String type = Iterables.getFirst(it, null);
        String modifier = Iterables.getLast(it, null);
        Map<String, Double> valueMap = (Map<String, Double>) kv.get("summary");
        Table<String> t = new Table<>("Statistic", "Value");
        for(Map.Entry<String, Double> summarykv : valueMap.entrySet()) {
          t.getTableModel().addRow(summarykv.getKey(), summarykv.getValue().longValue() + "");
        }
        ret.put(modifier + " " + type + " Distributional Summary", t );
      }
      return ret;
    }

    @Override
    public void run() {

      // Create panel to hold components
      final BasicWindow window = new BasicWindow();
      window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN));
      Panel panel = new Panel();
      panel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(2).setVerticalSpacing(2));
      panel.addComponent(new Label("Summary for " + column));
      panel.addComponent(new EmptySpace(new TerminalSize(0, 0))); // Empty space underneath labels
      panel.addComponent(new Label("Count Statistics"));
      panel.addComponent(getCountTable());
      for(Map.Entry<String, Table<String>> labelTable : getValueTable().entrySet()) {
        panel.addComponent(new Label(labelTable.getKey()));
        panel.addComponent(labelTable.getValue());
      }

      window.setCloseWindowWithEscape(true);

      // Create window to hold the panel
      window.setComponent(panel);
      gui.addWindow(window);
      gui.waitForWindowToClose(window);
      gui.addWindowAndWait(gui.getWindows().iterator().next());
    }
  }
}
