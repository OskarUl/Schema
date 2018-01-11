package com.example.hule_elev.schema;

        import android.graphics.RectF;
        import android.support.v4.view.MenuItemCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.support.v7.widget.Toolbar;
        import android.view.KeyEvent;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.Spinner;
        import android.widget.Toast;
        import android.graphics.Matrix;
        import android.graphics.PointF;
        import android.util.Log;
        import android.view.MotionEvent;

        import com.squareup.picasso.Picasso;



public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    public String SchoolId = "82710&code=465788";
    public String GroupId = "TE17A";
    public String week = "2";
    public String dlWidth = "1000";
    public String dlHeight = "2000";
    public ImageView imageView;
    public EditText editText;
    public String input;

        Toolbar toolbar;

    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        matrix = imageView.getImageMatrix();
        RectF drawableRect = new RectF(0, 0, 0, 0);
        RectF viewRect = new RectF(0, 0, imageView.getWidth(), imageView.getHeight());
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
        imageView.setImageMatrix(matrix);

        imageView.setOnTouchListener(this);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction()!= KeyEvent.ACTION_DOWN)
                    return false;
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (!editText.getText().toString().matches("")) {
                        GroupId = editText.getText().toString();
                    } else {
                        GroupId = "te17a";
                    }
                        System.out.println(GroupId);
                        loadImageFromUrl();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_bar, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weeks,
                R.layout.custom_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    spinner.setAdapter(adapter);

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Toast.makeText(MainActivity.this,
                    spinner.getSelectedItem().toString(),
                    Toast.LENGTH_SHORT)
                    .show();
            System.out.println(i);
            week = String.valueOf(i + 1);
            System.out.println(week);
            loadImageFromUrl();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    });
        return true;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;
        dumpEvent(event);
        // Handle touch events here..
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // first finger down only
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP:
                // first finger lifted

            case MotionEvent.ACTION_POINTER_UP:
                // second finger lifted
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                // first and second finger down
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) { savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_MOVE: if (mode == DRAG) {
                matrix.set(savedMatrix);
                matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                // create the transformation in the matrix of points
            } else if (mode == ZOOM) {
                // pinch zooming
                float newDist = spacing(event);
                Log.d(TAG, "newDist=" + newDist);
                if (newDist > 5f) {
                    matrix.set(savedMatrix);
                    scale = newDist / oldDist;
                    // setting the scaling of the
                    // matrix...if scale > 1 means
                    // zoom in...if scale < 1 means
                    // zoom out
                    matrix.postScale(scale, scale, mid.x, mid.y);
                    System.out.println(matrix);
                }
            }
                break;
        } view.setImageMatrix(matrix);
        // display the transformation on screen
        return true;
        // indicate event was handled
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
    /* --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------ */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) { String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT); sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount()) sb.append(";"); }
        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }


    private void loadImageFromUrl() {
        Picasso.with(this).load("http://www.novasoftware.se/ImgGen/schedulegenerator.aspx?format=png&schoolid="
                + SchoolId
                + "/sv-se&type=-1&id="
                + GroupId
                + "&period=&week="
                + week
                + "&mode=0&printer=1&colors=32&head=0&clock=0&foot=0&day=0&width="
                + dlWidth
                + "&height="
                + dlHeight
                + "&maxwidth="
                + dlWidth
                + "&maxheight="
                + dlHeight)
        .into(imageView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
    }
}


