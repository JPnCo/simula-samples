package fr.jpnco.simula.samples.dashboard.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.jpnco.simula.samples.dashboard.model.Sample;
import fr.jpnco.simula.samples.dashboard.model.TimeSeries;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvExporterTest {

  @TempDir Path tempDir;

  @Test
  void writes_header_and_one_line_per_sample() throws IOException {
    final TimeSeries series =
        TimeSeries.empty().append(new Sample(1, 10.0)).append(new Sample(2, 20.5));
    final Path path = tempDir.resolve("series.csv");
    CsvExporter.write(path, series);

    final String content = Files.readString(path, StandardCharsets.UTF_8);
    final String[] lines = content.split("\\r?\\n");
    assertEquals("simTime,value", lines[0]);
    assertEquals("1,10.0", lines[1]);
    assertEquals("2,20.5", lines[2]);
    assertEquals(3, lines.length);
  }

  @Test
  void writes_only_the_header_for_an_empty_series() throws IOException {
    final Path path = tempDir.resolve("empty.csv");
    CsvExporter.write(path, TimeSeries.empty());

    final String content = Files.readString(path, StandardCharsets.UTF_8);
    assertTrue(content.startsWith("simTime,value"));
  }

  @Test
  void creates_missing_parent_directories() throws IOException {
    final TimeSeries series = TimeSeries.empty().append(new Sample(1, 5.0));
    final Path path = tempDir.resolve("nested").resolve("out.csv");
    CsvExporter.write(path, series);
    assertTrue(Files.exists(path));
  }
}
