package ml.empee.mysticalBarriers.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

  public static BufferedReader buildReader(File file) throws IOException {
    return new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), Charset.defaultCharset()));
  }

  public static BufferedWriter buildWriter(File file) throws IOException {
    return new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), Charset.defaultCharset()));
  }

}
