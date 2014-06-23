package com.canonfer.ecatalog;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.util.Log;
import android.view.View;
import android.widget.Button;

 
@RunWith(RobolectricTestRunner.class)

public class MainActivityTest {

	private MainActivity sut;
	
	@Before
	public void setup() {
		sut = new MainActivity();
		Log.i("LogTag", "Before");
    } 	
	
    @Test 
    public void shouldHaveHappySmiles() throws Exception {
        String hello = sut.getResources().getString(R.string.dummy_button);
        assertThat(hello, equalTo("Dummy Button"));
    }
    
    @Test
    public void testButtonClick() throws Exception {
         
        Button view = (Button) sut.findViewById(R.id.dummy_button);
        assertNotNull(view);
        view.performClick();
        assertThat(Robolectric.shadowOf(view).getOnClickListener().getClass().toString(),equalTo(View.OnClickListener.class.toString()) );
 
 //       assertThat(equalTo());
      }

}