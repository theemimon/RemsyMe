package me.remsy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RedirectPolicyTest {

    @Test
    public void recognizesAllRedirectStatusCodes() {
        assertTrue(RedirectPolicy.isRedirectStatusCode(301));
        assertTrue(RedirectPolicy.isRedirectStatusCode(302));
        assertTrue(RedirectPolicy.isRedirectStatusCode(307));
        assertTrue(RedirectPolicy.isRedirectStatusCode(308));
    }

    @Test
    public void doesNotTreatSuccessOrErrorCodesAsRedirects() {
        assertFalse(RedirectPolicy.isRedirectStatusCode(200));
        assertFalse(RedirectPolicy.isRedirectStatusCode(404));
        assertFalse(RedirectPolicy.isRedirectStatusCode(500));
    }

    @Test
    public void nonRedirectResponseReturnsNull() {
        assertNull(RedirectPolicy.nextRedirectUrl(200, "https://example.com/next", 0));
    }

    @Test
    public void redirectWithoutLocationHeaderReturnsNull() {
        assertNull(RedirectPolicy.nextRedirectUrl(301, null, 0));
        assertNull(RedirectPolicy.nextRedirectUrl(301, "", 0));
    }

    @Test
    public void redirectWithLocationReturnsTargetUrl() {
        assertEquals("https://example.com/next", RedirectPolicy.nextRedirectUrl(301, "https://example.com/next", 0));
    }

    @Test
    public void throwsAfterMaxRedirectsReached() {
        try {
            RedirectPolicy.nextRedirectUrl(302, "https://example.com/loop", RedirectPolicy.MAX_REDIRECTS);
            fail("Expected TooManyRedirectsException");
        } catch (RedirectPolicy.TooManyRedirectsException expected) {
            // expected
        }
    }

    /**
     * Reproduces the original bug: a server that redirects to itself forever used to recurse
     * without bound. Following the policy in a loop must now stop deterministically.
     */
    @Test
    public void redirectLoopIsBoundedInsteadOfInfinite() {
        int redirectCount = 0;
        int iterations = 0;
        try {
            while (true) {
                String next = RedirectPolicy.nextRedirectUrl(302, "https://example.com/self", redirectCount);
                if (next == null) break;
                redirectCount++;
                iterations++;
                if (iterations > 10_000) {
                    fail("Redirect loop was not bounded");
                }
            }
            fail("Expected TooManyRedirectsException for a self-redirecting URL");
        } catch (RedirectPolicy.TooManyRedirectsException expected) {
            assertEquals(RedirectPolicy.MAX_REDIRECTS, redirectCount);
        }
    }
}
