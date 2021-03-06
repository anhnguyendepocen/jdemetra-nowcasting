/*
* Copyright 2013 National Bank of Belgium
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

package data;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.Tokenizer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class Data {
    public static Matrix readMatrix(String file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return readMatrix(reader);
        } catch (FileNotFoundException ex) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
            }
        }
    }

    public static Matrix readMatrix(Class cl, String resources) {
        InputStream stream = cl.getResourceAsStream(resources);
        return Data.readMatrix(stream);
    }

    public static Matrix readMatrix(InputStream stream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return readMatrix(reader);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
            }
        }
    }

    public static Matrix readMatrix(BufferedReader reader) {
        try {
            ArrayList<double[]> data = new ArrayList<>();
            DecimalFormat fmt = (DecimalFormat) DecimalFormat.getNumberInstance();
            char comma;
            if (fmt.getDecimalFormatSymbols().getDecimalSeparator() == ',') {
                comma = ';';
            } else {
                comma = ',';
            }

            String s = reader.readLine();
            while (s != null) {
                Tokenizer tokenizer = new Tokenizer(s, comma);
                ArrayList<Double> row = new ArrayList<>();
                while (tokenizer.hasNextToken()) {
                    String n = tokenizer.nextToken().trim();
                    if (n.isEmpty()) {
                        row.add(Double.NaN);
                    } else {
                        row.add(Double.parseDouble(n));
                    }
                }
                double[] x = new double[row.size()];
                for (int i = 0; i < x.length; ++i) {
                    x[i] = row.get(i);
                }
                data.add(x);
                s = reader.readLine();
            }
            int nrows = data.size();
            int ncols = 0;
            for (int i = 0; i < nrows; ++i) {
                int nc = data.get(i).length;
                if (nc > ncols) {
                    ncols = nc;
                }
            }
            Matrix M = new Matrix(nrows, ncols);
            M.set(Double.NaN);
            for (int i = 0; i < nrows; ++i) {
                M.row(i).copy(new DataBlock(data.get(i)));
            }
            return M;
        } catch (IOException err) {
            return null;
        } catch (NumberFormatException err) {
            return null;
        }
    }

    private static final double[] g_exports = {
        9568.3, 9920.3, 11353.5, 9247.5, 10114.2, 10763.1, 8456.1, 8071.6, 10328, 10551.4, 10186.1, 8821.6,
        9841.3, 10233.6, 10794.6, 10289.3, 10513.4, 10607.6, 9707.4, 8103.5, 10982.6, 11836.9, 10517.5, 9810.5,
        10374.8, 10855.3, 11671.3, 11901.2, 10846.4, 11917.5, 11362.8, 9314.5, 12605.9, 12815.1, 11254.5, 11111.8,
        11282.9, 11554.5, 12935.6, 12146.3, 11615.3, 13214.8, 11735.5, 9522.3, 12694.8, 12317.6, 11450, 11380.9,
        10604.6, 10972.2, 13331.5, 11733.1, 11284.7, 13295.8, 11881.4, 10374.2, 13828, 13490.5, 13092.2, 13184.4,
        12398.4, 13882.3, 15861.5, 13286.1, 15634.9, 14211, 13646.8, 12224.6, 15916.4, 16535.9, 15796, 14418.6,
        15044.5, 14944.2, 16754.8, 14254, 15454.9, 15644.8, 14568.3, 12520.2, 14803, 15873.2, 14755.3, 12875.1,
        14291.1, 14205.3, 15859.4, 15258.9, 15498.6, 15106.5, 15023.6, 12083, 15761.3, 16943, 15070.3, 13659.6,
        14768.9, 14725.1, 15998.1, 15370.6, 14956.9, 15469.7, 15101.8, 11703.7, 16283.6, 16726.5, 14968.9, 14861,
        14583.3, 15305.8, 17903.9, 16379.4, 15420.3, 17870.5, 15912.8, 13866.5, 17823.2, 17872, 17420.4, 16704.4,
        15991.5, 16583.6, 19123.4, 17838.8, 17335.3, 19026.9, 16428.6, 15337.4, 19379.8, 18070.5, 19563, 18190.6,
        17658, 18437.9, 21510.4, 17111, 19732.7, 20221.8
    };
    private static final double[] g_prod = {
        59.2, 58.3, 63.4, 59.7, 58.9, 62.7, 47.6, 58.6, 64.4, 66.4, 64.2, 62.2, 61.7, 62.2, 65.5, 64.6, 64.6, 62.2, 53.2, 62.5, 68.5, 73.5, 67.1, 68.6,
        69.1, 65.5, 72.7, 73, 70.3, 73.5, 61.5, 67.6, 77.7, 81.7, 73.5, 75.4, 70.6, 70.8, 76.9, 77.7, 71.1, 77.3, 63.1, 70.8, 80.5, 82.7, 75.8, 79.3,
        72.3, 74, 82.7, 79.1, 74.4, 79.5, 61.9, 73.5, 83.1, 82.9, 78, 80.4, 77.7, 79, 88.1, 79.5, 80.9, 85.7, 61.2, 78.7, 87.6, 91.5, 88.5, 86.6,
        86.8, 84.7, 94.1, 86.9, 90.2, 86.1, 68.8, 86.9, 90.7, 99.6, 94.9, 88.2, 95.2, 91.9, 97.5, 96.4, 95.2, 91.8, 74.7, 86.7, 96.2, 100.6, 89.7, 85.7,
        88.5, 83.8, 86.3, 86.7, 79, 84.2, 64.6, 72.6, 88.2, 91.1, 84, 85.8, 86.1, 88, 97.6, 95.3, 89.1, 93.5, 69.4, 86, 99.1, 97.3, 92.9, 92.7,
        90.2, 89.7, 102.3, 92, 89.1, 95.2, 67, 88.1, 95.6, 94.2, 93, 92.2, 91.5, 88.9, 99.1, 93.6, 91.5, 94.6, 67.6, 89.8, 99.3, 103.7, 100.3, 94.8,
        92.2, 93.8, 103.5, 98.8, 99.2, 99.5, 75.6, 96, 102.1, 109.3, 103.3, 96.3, 104.5, 102.8, 105.8, 102.3, 93.7, 99, 73, 87.9, 100.1, 103.8, 90.9, 89.1,
        91.6, 92.5, 100.3, 97.5, 90.4, 96.4, 70.8, 86.7, 102.5, 103.7, 96.8, 93.7, 93.4, 92.5, 99.9, 99.6, 91.5, 99.7, 70.6, 88.1, 102, 101.1, 94, 92.3,
        94.4, 93, 103.9, 96.1, 94.3, 102.2, 70, 93.5, 102.3, 102.5, 101.4, 94.5, 100.5, 100, 105.1, 96.3, 102.1, 97.8, 75.1, 94.3, 102, 110.4, 102.8, 92.9,
        99.4, 97.2, 105.5, 102.6, 99.7, 101, 79.6, 93.5, 107.7, 114, 104.5, 95.4, 104.1, 100.6, 104.6, 109, 95.7, 104.4, 82.5, 93.5, 109.6, 113.4, 100.6, 97.8,
        101.2, 101.7, 110.8, 108.7, 101.8, 107.2, 83, 97.5, 114.3, 116.4, 107.5, 101.5, 108.5, 109.3, 119, 111.3, 108.5, 117.5, 84.7, 107, 121.8, 117.7, 116, 108.5,
        118.4, 113, 122.5, 117.1, 112, 122.6, 90.2, 112.3, 122.4, 125.4, 120.7, 107.2, 126.8, 118.8, 132.9, 117.7, 121.8, 123.9, 90.3, 113.2, 124.7, 135.4, 126.3, 110.1,
        126.8, 117.7, 126.6, 123, 118.1, 123.7, 93.5, 105.4, 125, 131.9, 119.9, 110.3, 126.2, 121.6, 130.9, 123.6, 116.1, 126.9, 95, 107.6, 128.4, 127.1, 116.3, 109.5,
        113.4, 114, 128.5, 118.3, 108.6, 124.2, 86.7, 104.2, 124.1, 121.2, 112.6, 114.1, 120.3, 117.6, 133.6, 117.7, 113.8, 126.6, 81.6, 108.7, 125.9, 123, 120.7, 109.7};
    private static final double[] g_m1 = 
    {
        1320.7,1353.9,1604.3,1335.2,1365.7,1578.3,1160.5,1161.9,1450.8,1462,1431.5,1396.9,
        1375.6,1445.4,1548,1477.4,1488,1486.9,1260.4,1171.6,1585.7,1668.5,1446.5,1505.4,
        1399.5,1456.7,1554.9,1613.1,1543.5,1599.3,1402.7,1147.5,1629,1683.3,1415.2,1757,
        1497.5,1575.2,1718.8,1628.4,1446.6,1679.7,1456.2,1336.6,1714.3,1771.7,1662.6,1742.2,
        1461.3,1617.1,1826.8,1620.8,1514.3,1797.5,1500.6,1455.9,1743.9,1833,1761.5,1974.1,
        1671.8,1841.5,2126.9,1716.3,2005.8,1840.8,1733.4,1451.7,1957,2127.9,2094.3,2157.1,
        2160.3,1994.5,2225.3,2015.6,2044.5,2257.8,1810.4,1666.3,2235.2,2091.1,2093.9,1968.2,
        1962.3,2095.2,2161,2115.1,1929,2004.5,2009.9,1524.9,2061.1,2261.6,2103.6,2224.3,2173.8,
        2119.2,2226.4,2159.6,1918.3,2116.1,1948.3,1514.3,2180.5,2312.6,2019.8,2200.8,2028.9,
        2178.7,2433.7,2230.5,1884.2,2372.7,1918.6,1679.4,2327.3,2225.2,2211.7,2463.6,2029.5,
        2173.6,2387,2234,2179.9,2397,1960.2,1824.1,2479.3,2234.9,2345.9,2428.9,2179.4,2216.9,
        2642.3,2340.5,2474.6,2641.8,2165.1,1996.2,2562.9,2529.9,2549.6,2455.1,2472,2424.7,
        2820.1,2482.8,2509.8,2668.6,2498.3,2056.9,2559.4,2852.7,2465.9,2462.9,2577,2738.9,
        2771.8,2954.7,2525.3,3163.9,2720.1,2233.5,2972.4,2941.8,2171.7
    };
    private static final double[] g_m2 = 
    {
        1619.4,1655,1863.2,1595.3,1621.4,1761.1,1328.3,1547.5,1740.3,1727,1775.5,1778.5,1738.7,
        1798,2045,1808.6,1809.8,1897.7,1605.2,1730.8,2013,2061,1765.6,2083.2,1961.4,1960.6,
        2141.5,1961.6,1955.7,2126.3,1830.7,1835.9,2171.9,2262.8,2057.5,2350.5,1990.6,2027.2,
        2204.9,2020.3,1906.4,2034.8,1802.9,1724,2078.3,2020.4,1907.6,2055.7,1707.6,1801,
        2222.8,1996.1,1857,2164.3,1799.7,1946.1,2286.1,2287.2,2334,2640.6,2404.7,2540.2,2934.3,
        2533.3,2689.8,2596.7,2321.7,2746.6,2867.8,2881.4,3194,3146.5,2912.9,2924.3,2935.6,
        3022.4,2970.6,2862.1,2508,2636.3,2691.4,2717.7,2494.2,2599.7,2595.3,2576,2821.1,
        2807.3,2725.4,2617.5,2521.4,2463.2,2808.6,2993.2,2605.6,2856.4,2861.5,2809.3,3092.4,
        2671.3,2568.8,2656.9,2547.8,2408.4,2818.7,2871.5,2779.8,3009.6,2764,2788.5,3319.4,
        2998.2,2841.2,3233.5,2889.3,2910.5,3259.3,3419.9,3311.8,3644.9,3208.3,3400.6,3969.6,
        3657.2,3268.7,3486.7,3121.8,3544.2,3840.6,3725.7,4304.1,4887.5,4370,4343.9,5546,
        3953.4,4115.5,3964.8,3651,4032.1,3862.5,3993.1,3963,3962.3,3910.2,3685.9,4055.5,
        3584.7,4035.5,4188.1,4142.8,4142.1,4335.1,4792.7,4984.9,5027.9,5087.6,4881.2,5287.7,
        5299.5,5075.3,5779.7,5245.9,5103.1,5285.6,5221.1,4348.7
    };
    private static final double[] g_m3 = 
    {
        1661.8,1736.9,2233.7,1925,1938.8,2017.7,1442.7,1673.8,1887.8,1957.7,1930.4,1737.8,
        1815.1,1888.1,1950.6,1806.2,1746.8,1778.2,1502,1541.1,1876.6,1979.7,1777.5,1716.7,
        1689.9,1805.9,2006.4,2004.9,1740,2014.5,1639.4,1561,2000.7,1968.2,1825.5,1846.9,
        1714.8,1936.7,2194.8,2105.3,1949.7,2150.1,1864.1,1873.6,2107.7,2077.4,2007.4,1975.8,
        1737,1844.1,2216.8,1982,1816.9,2155.1,1632.5,1851,2147.4,2163.3,2192,2251.7,2004.3,
        2429.2,2641.8,2203.7,2504.1,2280.4,2054.3,2185.2,2406.8,2437.7,2606.2,2350.6,2386.5,
        2469.6,2785.1,2334.1,2388.2,2379.8,2003.1,2023.9,2276.4,2420.3,2361.2,2241.3,2171.7,
        2293.9,2493.7,2382.5,2286,2391.8,2163.6,2095.9,2442.1,2611.2,2498.6,2342.2,2326.8,
        2417.4,2572.8,2403.6,2294.1,2353.6,2201.2,1925.8,2428.8,2603.2,2330.1,2482.9,2255.6,
        2518,2960.7,2571.5,2348.4,2817.9,2166.6,2284.7,2864.3,2738.3,2734.9,2893.3,2503.3,
        2685.1,3034.9,2826.9,2529.1,2867.3,2202.9,2401.3,2869.8,2589,2945.2,2896.6,2809.3,
        2926.4,3634.7,2772.1,3023.5,3022.9,2565.5,2797.5,3101,3092.9,3140.5,2751.5,2947.4,
        3128.4,3569,2991.1,3217.3,3309.6,2924.5,2881.1,3113.6,3350.3,3236.7,3058.2,3330,
        3437.9,3536.9,3707.8,3316.2,3697.6,3199.1,2929.6,3468.5,3620.7,3065.6
    };

    public static final TsData X = new TsData(TsFrequency.Monthly, 1995, 0, g_exports, false);
    public static final TsData P = new TsData(TsFrequency.Monthly, 1967, 0, g_prod, false);
    public static final TsData M1 = new TsData(TsFrequency.Monthly, 1995, 0, g_m1, false);
    public static final TsData M2 = new TsData(TsFrequency.Monthly, 1995, 0, g_m2, false);
    public static final TsData M3= new TsData(TsFrequency.Monthly, 1995, 0, g_m3, false);

    /**
     * *
     * Generates monthly random airline series
     *
     * @param nseries Number of series
     * @param length Length of the series
     * @param th Regular ma parameter (true sign)
     * @param bth Seasonal ma parameter (true sign)
     * @return
     */
    public static List<TsData> rndAirlines(int nseries, int length, double th, double bth) {
        TsData[] rnd = new TsData[nseries];
        SarimaModelBuilder builder = new SarimaModelBuilder();
        ArimaModelBuilder gen = new ArimaModelBuilder();
        for (int i = 0; i < rnd.length; ++i) {
            SarimaModel airline = builder.createAirlineModel(12, th, bth);
            double[] vals = gen.generate(airline, length);
            rnd[i] = new TsData(TsFrequency.Monthly, 2000, 0, vals, false);
        }
        return Arrays.asList(rnd);
    }
}
