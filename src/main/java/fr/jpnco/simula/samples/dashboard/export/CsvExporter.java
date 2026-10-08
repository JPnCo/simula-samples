package fr.jpnco.simula.samples.dashboard.export;

import fr.jpnco.simula.samples.dashboard.model.Sample;
import fr.jpnco.simula.samples.dashboard.model.TimeSeries;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes a collected {@link TimeSeries} to a machine-readable CSV file (FR-006, SC-005).
 *
 * <p>The file starts with a header line and then contains one {@code simTime,value} line per
 * recorded sample. An empty series writes only the header line and is still valid, so the sample
 * reports "nothing was exported" rather than producing a malformed file (FR-008). All lines use
 * {@link System#lineSeparator()} as the terminator.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-006, FR-008, SC-005.
 */
public final class CsvExporter {

  /** The header line written at the top of the export file. */
  private static final String HEADER = "simTime,value";

  /** Private constructor to prevent instantiation of this utility class. */
  private CsvExporter() {}

  /**
   * Writes the given series to the file at the given path, creating any missing parent directories.
   * Participates in: FR-006, FR-008, SC-005.
   *
   * @param path the destination file path
   * @param series the series to write
   * @throws UncheckedIOException if the file cannot be written
   */
  public static void write(final Path path, final TimeSeries series) {
    try {
      final List<String> lines = new ArrayList<>();
      lines.add(HEADER);
      for (final Sample sample : series.getSamples()) {
        lines.add(sample.getSimTime() + "," + sample.getValue());
      }
      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }
      Files.write(path, lines, StandardCharsets.UTF_8);
    } catch (final IOException exc) {
      throw new UncheckedIOException("Failed to export dashboard series to " + path, exc);
    }
  }
}
