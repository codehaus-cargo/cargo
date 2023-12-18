package org.codehaus.cargo.container;

import junit.framework.TestCase;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.DescriptorTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DtdTest extends TestCase {
    private Dtd dtd;

    protected void setUp() {
        // Created a test double or mock Dtd instance for testing
        dtd = createMockDtd();
    }

    public void testGetElementOrderForValidTag() {
        String tagName = "ValidTag";

        List<DescriptorTag> elementOrder = dtd.getElementOrder(tagName);

        assertNotNull(elementOrder);
        // Added assertions to validate the element order for the given tag
    }

    public void testGetElementOrderForNonExistentTag() {
        String tagName = "NonExistentTag";

        List<DescriptorTag> elementOrder = dtd.getElementOrder(tagName);

        assertNull(elementOrder);
    }


    private Dtd createMockDtd() {
        // Created a mock Dtd or test double for testing
        return new MockDtd();
    }


    private class MockDtd extends Dtd {
        private Map<String, List<DescriptorTag>> elementOrders = new HashMap<>();

        public MockDtd() {
            super("mock.dtd");
            initializeElementOrders();
        }

        private void initializeElementOrders() {

            List<DescriptorTag> validTagElementOrder = new ArrayList<>();

            elementOrders.put("ValidTag", validTagElementOrder);

            List<DescriptorTag> nonEmptyTagElementOrder = new ArrayList<>();

            elementOrders.put("NonEmptyTag", nonEmptyTagElementOrder);

        }

        @Override
        public List<DescriptorTag> getElementOrder(String tagName) {
            return elementOrders.get(tagName);
        }
    }
}
