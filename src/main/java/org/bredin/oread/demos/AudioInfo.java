package org.bredin.oread.demos;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

/**
 * List audio system information:
 * mvn exec:java -Dexec.mainClass=org.bredin.oread.demos.AudioInfo
 */
public class AudioInfo {
  /** Entry point.  No CLI arguments, yet. */
  public static void main(String[] args) throws Exception {
    System.out.println("File types:");
    for (AudioFileFormat.Type t: AudioSystem.getAudioFileTypes()) {
      System.out.println(" " + t);
    }
    System.out.println("Audio inputs:");
    for (Mixer.Info i: AudioSystem.getMixerInfo()) {
      System.out.println(" " + i);
    }

  }
}
