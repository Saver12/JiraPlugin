package ut.com.example.plugins.tutorial.crud;

import org.junit.Test;
import com.epam.plugins.statusreport.api.MyPluginComponent;
import com.epam.plugins.statusreport.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}