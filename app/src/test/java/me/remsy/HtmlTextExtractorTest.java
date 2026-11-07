package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HtmlTextExtractorTest {

    @Test
    public void returnsEmptyStringForNullInput() {
        assertEquals("", HtmlTextExtractor.extractRawTextFromHtml(null));
    }

    @Test
    public void stripsScriptStyleAndNavLikeBlocks() {
        String html = "<html><head><style>body{color:red}</style></head>"
                + "<body><script>var a=1;</script><nav>Home About</nav>"
                + "<header>Site Header</header>"
                + "<h1>Amazing Cake</h1><p>Mix it well</p>"
                + "<footer>Copyright 2026</footer></body></html>";

        String result = HtmlTextExtractor.extractRawTextFromHtml(html);

        assertFalse(result.contains("color:red"));
        assertFalse(result.contains("var a=1"));
        assertFalse(result.contains("Home About"));
        assertFalse(result.contains("Site Header"));
        assertFalse(result.contains("Copyright 2026"));
        assertTrue(result.contains("Amazing Cake"));
        assertTrue(result.contains("Mix it well"));
    }

    @Test
    public void convertsBlockTagsToNewlinesAndTrimsWhitespace() {
        String html = "<div>Line One</div><div>Line   Two</div>";
        String result = HtmlTextExtractor.extractRawTextFromHtml(html);
        assertEquals("Line One\nLine Two", result);
    }

    @Test
    public void decodesCommonHtmlEntities() {
        String html = "<p>Salt &amp; Pepper &ndash; to taste, 1&frac12; cups</p>";
        String result = HtmlTextExtractor.extractRawTextFromHtml(html);
        assertEquals("Salt & Pepper - to taste, 11/2 cups", result);
    }
}
