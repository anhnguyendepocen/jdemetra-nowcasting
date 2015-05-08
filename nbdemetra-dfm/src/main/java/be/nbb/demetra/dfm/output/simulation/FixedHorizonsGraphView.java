/*
 * Copyright 2014 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package be.nbb.demetra.dfm.output.simulation;

import be.nbb.demetra.dfm.output.simulation.utils.FilterHorizonsPanel;
import com.google.common.base.Optional;
import ec.nbdemetra.ui.DemetraUI;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tss.datatransfer.TsDragRenderer;
import ec.tss.datatransfer.TssTransferSupport;
import ec.tss.dfm.DfmDocument;
import ec.tss.dfm.DfmSeriesDescriptor;
import ec.tss.dfm.DfmSimulation;
import ec.tss.dfm.DfmSimulationResults;
import ec.tss.tsproviders.utils.Formatters;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataCollector;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.ui.chart.TsXYDatasets;
import ec.util.chart.ColorScheme;
import ec.util.chart.ObsFunction;
import ec.util.chart.SeriesFunction;
import ec.util.chart.TimeSeriesChart;
import ec.util.chart.swing.SwingColorSchemeSupport;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY;

/**
 *
 * @author Mats Maggi
 */
public class FixedHorizonsGraphView extends javax.swing.JPanel {

    public static final String DFM_SIMULATION_PROPERTY = "dfmSimulation";

    private Optional<DfmSimulation> dfmSimulation;

    private final DemetraUI demetraUI;
    private Formatters.Formatter<Number> formatter;
    private SwingColorSchemeSupport defaultColorSchemeSupport;
    private TsCollection collection;

    private DfmDocument document;
    private FilterHorizonsPanel filterPanel;

    /**
     * Creates new form FixedHorizonsGraphView
     */
    public FixedHorizonsGraphView(DfmDocument doc) {
        initComponents();
        this.document = doc;

        demetraUI = DemetraUI.getDefault();
        formatter = demetraUI.getDataFormat().numberFormatter();
        defaultColorSchemeSupport = new SwingColorSchemeSupport() {
            @Override
            public ColorScheme getColorScheme() {
                return demetraUI.getColorScheme();
            }
        };

        this.dfmSimulation = Optional.absent();

        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                filterPanel = null;
                updateChart();
            }
        });

        typeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateChart();
            }
        });

        chart.setValueFormat(new DecimalFormat("#.###"));
        chart.setSeriesRenderer(new SeriesFunction<TimeSeriesChart.RendererType>() {
            @Override
            public TimeSeriesChart.RendererType apply(int series) {
                switch (series) {
                    case 0:
                        return TimeSeriesChart.RendererType.STACKED_COLUMN;
                    default:
                        return TimeSeriesChart.RendererType.LINE;

                }
            }
        });
        chart.setSeriesFormatter(new SeriesFunction<String>() {
            @Override
            public String apply(int series) {
                return chart.getDataset().getSeriesKey(series).toString();
            }
        });
        chart.setObsFormatter(new ObsFunction<String>() {
            @Override
            public String apply(int series, int obs) {
                return chart.getSeriesFormatter().apply(series)
                        + "\n" + chart.getPeriodFormat().format(chart.getDataset().getX(series, obs))
                        + "\n" + formatter.format(chart.getDataset().getY(series, obs));
            }
        });

        chart.setColorSchemeSupport(defaultColorSchemeSupport);
        chart.setNoDataMessage("No data produced");
        chart.setMouseWheelEnabled(true);

        chart.setTransferHandler(new TsCollectionTransferHandler());

        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                switch (evt.getPropertyName()) {
                    case DFM_SIMULATION_PROPERTY:
                        updateComboBox();
                        updateChart();
                        break;
                }
            }
        });

        updateComboBox();
        updateChart();

        demetraUI.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                switch (evt.getPropertyName()) {
                    case DemetraUI.DATA_FORMAT_PROPERTY:
                        onDataFormatChanged();
                        break;
                    case DemetraUI.COLOR_SCHEME_NAME_PROPERTY:
                        onColorSchemeChanged();
                        break;
                }
            }
        });
    }

    private void onDataFormatChanged() {
        formatter = demetraUI.getDataFormat().numberFormatter();
    }

    private void onColorSchemeChanged() {
        defaultColorSchemeSupport = new SwingColorSchemeSupport() {
            @Override
            public ColorScheme getColorScheme() {
                return demetraUI.getColorScheme();
            }
        };
        chart.setColorSchemeSupport(defaultColorSchemeSupport);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public Optional<DfmSimulation> getSimulationResults() {
        return dfmSimulation;
    }

    public void setSimulationResults(Optional<DfmSimulation> dfmSimulation) {
        Optional<DfmSimulation> old = this.dfmSimulation;
        this.dfmSimulation = dfmSimulation != null ? dfmSimulation : Optional.<DfmSimulation>absent();
        firePropertyChange(DFM_SIMULATION_PROPERTY, old, this.dfmSimulation);
    }
    //</editor-fold>

    private void updateChart() {
        if (dfmSimulation.isPresent()
                && comboBox.getSelectedIndex() != -1
                && typeComboBox.getSelectedIndex() != -1) {
            TsXYDatasets.Builder b = TsXYDatasets.builder();
            collection = toCollection(dfmSimulation.get());
            for (Ts o : collection) {
                b.add(o.getName(), o.getTsData());
            }
            chart.setDataset(b.build());
        } else {
            chart.setDataset(null);
        }
    }

    private void updateComboBox() {
        filterPanel = null;
        if (dfmSimulation.isPresent()) {
            comboBox.setModel(toComboBoxModel(document.getDfmResults().getDescriptions()));
            comboBox.setEnabled(true);
        } else {
            comboBox.setModel(new DefaultComboBoxModel());
            comboBox.setEnabled(false);
        }
    }

    private static DefaultComboBoxModel toComboBoxModel(DfmSeriesDescriptor[] data) {
        DefaultComboBoxModel result = new DefaultComboBoxModel(data);
        return result;
    }

    private TsCollection toCollection(DfmSimulation dfmSimulation) {
        Objects.requireNonNull(dfmSimulation);

        TsCollection result = TsFactory.instance.createTsCollection();

        int selectedIndex = comboBox.getSelectedIndex();
        DfmSimulationResults dfm = dfmSimulation.getDfmResults().get(selectedIndex);
        int type = typeComboBox.getSelectedIndex();
        List<Double> trueValues = type == 1 ? dfm.getTrueValuesYoY() : type == 2 ? dfm.getTrueValuesQoQ() : dfm.getTrueValues();
        Double[][] fcts = type == 1 ? dfm.getForecastsArrayYoY() : type == 2 ? dfm.getForecastsArrayQoQ() : dfm.getForecastsArray();
        List<TsPeriod> periods = dfm.getEvaluationSample();
        List<Integer> horizons = dfm.getForecastHorizons();

        if (filterPanel == null) {
            filterPanel = new FilterHorizonsPanel(null, horizons);
        }

        TsFrequency freq = periods.get(0).getFrequency();

        TsDataCollector coll = new TsDataCollector();
        for (int i = 0; i < periods.size(); i++) {
            if (trueValues.get(i) != null && trueValues.get(i) != Double.NaN) {
                coll.addObservation(periods.get(i).middle(), trueValues.get(i));
            } else {
                coll.addMissingValue(periods.get(i).middle());
            }
        }
        TsData trueTsData = coll.make(freq, TsAggregationType.None);

        result.quietAdd(TsFactory.instance.createTs("True data", null, trueTsData));

        // Add horizons
        SortedSet<Integer> filteredHorizons = filterPanel.getSelectedElements();
        for (int i = 0; i < horizons.size(); i++) {
            if (filteredHorizons.contains(horizons.get(i))) {
                coll.clear();
                for (int j = 0; j < periods.size(); j++) {
                    if (fcts[i][j] != null && fcts[i][j] != Double.NaN) {
                        coll.addObservation(periods.get(j).middle(), fcts[i][j]);
                    } else {
                        coll.addMissingValue(periods.get(j).middle());
                    }
                }
                result.quietAdd(TsFactory.instance.createTs("fh(" + horizons.get(i) + ")", null, coll.make(freq, TsAggregationType.None)));
            }

        }
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        comboBoxPanel = new javax.swing.JPanel();
        variableLabel = new javax.swing.JLabel();
        comboBox = new javax.swing.JComboBox();
        variableLabel1 = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        filterButton = new javax.swing.JButton();
        chart = new ec.util.chart.swing.JTimeSeriesChart();

        setLayout(new java.awt.BorderLayout());

        comboBoxPanel.setLayout(new javax.swing.BoxLayout(comboBoxPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(variableLabel, org.openide.util.NbBundle.getMessage(FixedHorizonsGraphView.class, "FixedHorizonsGraphView.variableLabel.text")); // NOI18N
        variableLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 10));
        comboBoxPanel.add(variableLabel);

        comboBoxPanel.add(comboBox);

        org.openide.awt.Mnemonics.setLocalizedText(variableLabel1, org.openide.util.NbBundle.getMessage(FixedHorizonsGraphView.class, "FixedHorizonsGraphView.variableLabel1.text")); // NOI18N
        variableLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 10));
        comboBoxPanel.add(variableLabel1);

        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Level", "Year On Year", "Quarter On Quarter" }));
        comboBoxPanel.add(typeComboBox);

        org.openide.awt.Mnemonics.setLocalizedText(filterButton, org.openide.util.NbBundle.getMessage(FixedHorizonsGraphView.class, "FixedHorizonsGraphView.filterButton.text")); // NOI18N
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });
        comboBoxPanel.add(filterButton);

        add(comboBoxPanel, java.awt.BorderLayout.NORTH);

        chart.setNoDataMessage(org.openide.util.NbBundle.getMessage(FixedHorizonsGraphView.class, "FixedHorizonsGraphView.chart.noDataMessage")); // NOI18N
        add(chart, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        int r = JOptionPane.showConfirmDialog(chart, filterPanel, "Select horizons to display", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            updateChart();
        }
    }//GEN-LAST:event_filterButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ec.util.chart.swing.JTimeSeriesChart chart;
    private javax.swing.JComboBox comboBox;
    private javax.swing.JPanel comboBoxPanel;
    private javax.swing.JButton filterButton;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel variableLabel;
    private javax.swing.JLabel variableLabel1;
    // End of variables declaration//GEN-END:variables
    private TsCollection dragSelection = null;

    protected Transferable transferableOnSelection() {
        TsCollection col = TsFactory.instance.createTsCollection();

        ListSelectionModel model = chart.getSeriesSelectionModel();
        if (!model.isSelectionEmpty()) {
            for (int i = model.getMinSelectionIndex(); i <= model.getMaxSelectionIndex(); i++) {
                if (model.isSelectedIndex(i)) {
                    col.quietAdd(collection.get(i));
                }
            }
        }
        dragSelection = col;
        return TssTransferSupport.getDefault().fromTsCollection(dragSelection);
    }

    public class TsCollectionTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            transferableOnSelection();
            TsDragRenderer r = dragSelection.getCount() < 10 ? TsDragRenderer.asChart() : TsDragRenderer.asCount();
            Image image = r.getTsDragRendererImage(Arrays.asList(dragSelection.toArray()));
            setDragImage(image);
            return COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return transferableOnSelection();
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return false;
        }
    }
}