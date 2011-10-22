
package com.twitter;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import org.ho.yaml.Yaml;

public class ConformanceTest extends TestCase {

  private static final String CONFORMANCE_DIR_PROPERTY = "conformance.dir";
  protected static final String KEY_DESCRIPTION = "description";
  protected static final String KEY_INPUT = "text";
  protected static final String KEY_EXPECTED_OUTPUT = "expected";
  protected static final String KEY_HIGHLIGHT_HITS = "hits";
  protected File conformanceDir;
  protected Extractor extractor = new Extractor();
  protected Autolink linker = new Autolink();
  protected HitHighlighter hitHighlighter = new HitHighlighter();

  public void testMentionsExtractor() throws Exception {
    File yamlFile = new File(conformanceDir, "extract.yml");
    List testCases = loadConformanceData(yamlFile, "mentions");
    for (Map testCase : (List<Map>)testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (List)testCase.get(KEY_EXPECTED_OUTPUT),
                   extractor.extractMentionedScreennames((String)testCase.get(KEY_INPUT)));
    }
  }

  public void testReplyExtractor() throws Exception {
    File yamlFile = new File(conformanceDir, "extract.yml");
    List testCases = loadConformanceData(yamlFile, "replies");
    for (Map testCase : (List<Map>)testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (String)testCase.get(KEY_EXPECTED_OUTPUT),
                   extractor.extractReplyScreenname((String)testCase.get(KEY_INPUT)));
    }
  }

  public void testHashtagsExtractor() throws Exception {
    File yamlFile = new File(conformanceDir, "extract.yml");
    List testCases = loadConformanceData(yamlFile, "hashtags");
    for (Map testCase : (List<Map>)testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (List)testCase.get(KEY_EXPECTED_OUTPUT),
                   extractor.extractHashtags((String)testCase.get(KEY_INPUT)));
    }
  }

  public void testHashtagsWithIndicesExtractor() throws Exception {
    File yamlFile = new File(conformanceDir, "extract.yml");
    List testCases = loadConformanceData(yamlFile, "hashtags_with_indices");
    for (Map testCase : (List<Map>)testCases) {
      List<Map<String, Object>> expectedConfig = (List)testCase.get(KEY_EXPECTED_OUTPUT);
      List<Extractor.Entity> expected = new ArrayList<Extractor.Entity>();
      for (Map<String, Object> configEntry : expectedConfig) {
        expected.add(new Extractor.Entity(configEntry, "hashtag"));
      }

      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   expected,
                   extractor.extractHashtagsWithIndices((String)testCase.get(KEY_INPUT)));
    }
  }

  public void testURLsExtractor() throws Exception {
    File yamlFile = new File(conformanceDir, "extract.yml");
    List testCases = loadConformanceData(yamlFile, "urls");
    for (Map testCase : (List<Map>)testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (List)testCase.get(KEY_EXPECTED_OUTPUT),
                   extractor.extractURLs((String)testCase.get(KEY_INPUT)));
    }
  }


  public void testUsernameAutolinking() throws Exception {
    File yamlFile = new File(conformanceDir, "autolink.yml");
    List testCases = loadConformanceData(yamlFile, "usernames");
    for (Map testCase : (List<Map>)testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (String)testCase.get(KEY_EXPECTED_OUTPUT),
                   linker.autoLinkUsernamesAndLists((String)testCase.get(KEY_INPUT)));
    }
  }

  public void testListAutolinking() throws Exception {
    File yamlFile = new File(conformanceDir, "autolink.yml");
    List testCases = loadConformanceData(yamlFile, "lists");
    for (Map testCase : (List<Map>)testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (String)testCase.get(KEY_EXPECTED_OUTPUT),
                   linker.autoLinkUsernamesAndLists((String) testCase.get(KEY_INPUT)));
    }
  }

  public void testHashtagAutolinking() throws Exception {
    File yamlFile = new File(conformanceDir, "autolink.yml");
    List testCases = loadConformanceData(yamlFile, "hashtags");
    for (Map testCase : (List<Map>)testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (String)testCase.get(KEY_EXPECTED_OUTPUT),
                   linker.autoLinkHashtags((String) testCase.get(KEY_INPUT)));
    }
  }

  public void testURLAutolinking() throws Exception {
    File yamlFile = new File(conformanceDir, "autolink.yml");
    List testCases = loadConformanceData(yamlFile, "urls");
    for (Map testCase : (List<Map>)testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (String)testCase.get(KEY_EXPECTED_OUTPUT),
                   linker.autoLinkURLs((String)testCase.get(KEY_INPUT)));
    }
  }

  public void testAllAutolinking() throws Exception {
    File yamlFile = new File(conformanceDir, "autolink.yml");
    List testCases = loadConformanceData(yamlFile, "all");
    autolink(testCases);
  }

  public void testPlainTextHitHighlighting() throws Exception {
    File yamlFile = new File(conformanceDir, "hit_highlighting.yml");
    List testCases = loadConformanceData(yamlFile, "plain_text");
    List<List<Integer>> hits = null;
    
    for (Map testCase : (List<Map>)testCases) {
      hits = (List<List<Integer>>)testCase.get(KEY_HIGHLIGHT_HITS);
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (String)testCase.get(KEY_EXPECTED_OUTPUT),
                   hitHighlighter.highlight( (String)testCase.get(KEY_INPUT), hits));
    }
  }

  public void setUp() {
    assertNotNull("Missing required system property: " + CONFORMANCE_DIR_PROPERTY, System.getProperty(
            CONFORMANCE_DIR_PROPERTY));
    conformanceDir = new File(System.getProperty(CONFORMANCE_DIR_PROPERTY));
    assertTrue("Conformance directory " + conformanceDir + " is not a directory.", conformanceDir.isDirectory());

    assertNotNull("No extractor configured", extractor);
    assertNotNull("No autolinker configured", linker);
    linker.setNoFollow(false);
  }

  protected void autolink(List<Map> testCases) {
    for (Map testCase : testCases) {
      assertEquals((String)testCase.get(KEY_DESCRIPTION),
                   (String)testCase.get(KEY_EXPECTED_OUTPUT),
                   linker.autoLink((String) testCase.get(KEY_INPUT)));
    }
  }

  protected List loadConformanceData(File yamlFile, String testType) throws FileNotFoundException {
    Map fullConfig = (Map) Yaml.load(yamlFile);
    Map testConfig = (Map)fullConfig.get("tests");
    return (List)testConfig.get(testType);
  }
}
