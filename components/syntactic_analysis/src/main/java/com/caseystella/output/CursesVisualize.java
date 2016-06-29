package com.caseystella.output;

import com.caseystella.summarize.Summary;
import com.caseystella.summarize.TotalSummary;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
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
import java.text.NumberFormat;
import java.util.*;

public class CursesVisualize {

  public void display(final TotalSummary totalSummary) throws IOException {
    // Setup terminal and screen layers
    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    Screen screen = new TerminalScreen(terminal);
    screen.startScreen();
    Table<String> connectedColumns = new Table<>("Col 1", "Col 2");
    connectedColumns.setVisibleRows(10);

    for(Map<String, Object> columnConn : totalSummary.getConnectedColumns()) {
      String col1 = (String)columnConn.get("column 1");
      String col2 = (String)columnConn.get("column 2");
      connectedColumns.getTableModel().addRow(col1 + "  ", col2);
    }

    // Create gui and start gui
    final MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));


    final BasicWindow window = new BasicWindow();
    window.setHints(Arrays.asList(Window.Hint.CENTERED));
    window.setCloseWindowWithEscape(true);
    Panel panel = new Panel();
    panel.setLayoutManager(new GridLayout(1).setVerticalSpacing(1).setHorizontalSpacing(0));
    /*
    Panel panel = new Panel();
    final ComboBox<String> comboBox = new ComboBox<String>().setReadOnly(false);
    for(Map.Entry<String, Summary> kv : totalSummary.getColumnSummaries().entrySet()) {
      comboBox.addItem(kv.getKey());
    }
    Button button = new Button("Show", new Runnable() {
      @Override
      public void run() {
        String column = comboBox.getSelectedItem();
        Summary summary = totalSummary.getColumnSummaries().get(column);
        new ColumnDisplayer(gui, summary, column).run();
      }
    });
    panel.addComponent(new Label("Column Statistical Details"));
    panel.addComponent(new EmptySpace(new TerminalSize(0, 0))); // Empty space underneath labels
    panel.addComponent(comboBox);
    panel.addComponent(button);
    panel.addComponent(new Label("Interesting Connections\nBased on Loglikelihood"));
    panel.addComponent(new EmptySpace(new TerminalSize(0,0))); // Empty space underneath labels
    panel.addComponent(connectedColumns);
    */
    ActionListBox actionListBox = new ActionListBox();
    NumberFormat percentFormatter;

    percentFormatter = NumberFormat.getPercentInstance();
    for(Map.Entry<String, Summary> kv : totalSummary.getColumnSummaries().entrySet()) {
      double percentMissing = kv.getValue().getNumInvalid().doubleValue() / kv.getValue().getTotalCount();
      String actionName = kv.getKey() + " (" +percentFormatter.format(percentMissing) + " Missing)";
      actionListBox.addItem(actionName, new ColumnDisplayer(gui, kv.getValue(), kv.getKey()));
    }
    panel.addComponent(new Label("Column Statistical Details"));
    panel.addComponent(actionListBox);
    window.setComponent(panel);
    gui.addWindowAndWait(window);
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
      table.setVisibleRows(5);
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
    public Table<String> getSimilarityTable(Map<String, String> entries) {
      Table<String> t = new Table<>("word", "synonym");
      t.setVisibleRows(5);
      for(Map.Entry<String, String> synonyms : entries.entrySet()) {
        t.getTableModel().addRow(trim(synonyms.getKey(), 20), trim(synonyms.getValue(), 20));
      }
      return t;
    }

    public static String trim(String s, int numChars) {
      if(s.length() > numChars) {
        return s.substring(0, numChars) + "...";
      }
      return s;
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
        t.setVisibleRows(5);
        for(Map.Entry<String, Double> summarykv : valueMap.entrySet()) {
          t.getTableModel().addRow(summarykv.getKey(), summarykv.getValue().longValue() + "");
        }
        ret.put("Canonical Representation Count\n" + modifier + " " + type  , t );
      }
      for(Map<String, Object> kv : columnSummary.getNumericValueSummary()) {
        String typeMod = kv.get("type").toString();
        Iterable<String> it = Splitter.on(":").split(typeMod);
        String type = Iterables.getFirst(it, null);
        String modifier = Iterables.getLast(it, null);
        Map<String, Double> valueMap = new TreeMap<>((Map<String, Double>) kv.get("summary"));
        Table<String> t = new Table<>("Statistic", "Value");
        t.setVisibleRows(5);
        for(Map.Entry<String, Double> summarykv : valueMap.entrySet()) {
          t.getTableModel().addRow(summarykv.getKey(), summarykv.getValue().longValue() + "");
        }
        ret.put("Distributional Summary\n" + modifier + " " + type, t );
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
      panel.addComponent(new Label("Possible Value Synonymns"));
      panel.addComponent(getSimilarityTable(columnSummary.getSynonyms()));
      window.setCloseWindowWithEscape(true);

      // Create window to hold the panel
      window.setComponent(panel);
      gui.addWindow(window);
      gui.waitForWindowToClose(window);
      gui.addWindowAndWait(gui.getWindows().iterator().next());
    }
  }
}
