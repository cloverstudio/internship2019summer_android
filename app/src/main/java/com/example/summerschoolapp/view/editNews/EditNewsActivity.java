package com.example.summerschoolapp.view.editNews;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.lifecycle.ViewModelProviders;

import com.example.summerschoolapp.R;
import com.example.summerschoolapp.common.BaseActivity;
import com.example.summerschoolapp.common.BaseError;
import com.example.summerschoolapp.dialog.ErrorDialog;
import com.example.summerschoolapp.dialog.SuccessDialog;
import com.example.summerschoolapp.errors.NewUserError;
import com.example.summerschoolapp.model.News;
import com.example.summerschoolapp.utils.Const;
import com.example.summerschoolapp.utils.helpers.EventObserver;
import com.example.summerschoolapp.utils.helpers.ScrollAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

public class EditNewsActivity extends BaseActivity {

    public static void StartActivity(Context context, News newsForEditing) {

        Intent intent = new Intent(context, EditNewsActivity.class);
        intent.putExtra(Const.Intent.NEWS_DATA, newsForEditing);
        context.startActivity(intent);
    }

    @BindView(R.id.et_news_address)
    EditText etNewsAddress;

    @BindView(R.id.et_news_title)
    EditText etNewsTitle;

    @BindView(R.id.et_news_text)
    EditText etNewsText;

    @BindView(R.id.ibtn_upload_document)
    ImageButton ibtnUploadDocument;

    @BindView(R.id.ibtn_upload_photo)
    ImageButton ibtnUploadPhoto;

    @BindView(R.id.sv_news_item)
    ScrollAdapter svNewsItem;

    @BindView(R.id.mv_news_location)
    MapView mapView;

    private EditNewsViewModel viewModel;
    private GoogleMap mMap;
    News newsForEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_news);
        ButterKnife.bind(this);

        //TODO What happened to ID?
        if (getIntent() != null && getIntent().getExtras() != null) {
            newsForEditing = getIntent().getParcelableExtra(Const.Intent.NEWS_DATA);
        }

        viewModel = ViewModelProviders.of(this).get(EditNewsViewModel.class);

        viewModel.getProgressStatus().observe(this, progressStatus -> {
            switch (progressStatus) {
                case START_PROGRESS:
                    showProgress();
                    break;
                case STOP_PROGRESS:
                    hideProgress();
                    break;
            }
        });

        viewModel.getBaseErrors().observe(this, new EventObserver<BaseError>() {
            @Override
            public void onEventUnhandledContent(BaseError value) {
                super.onEventUnhandledContent(value);
                String message = getString(R.string.text_try_again);
                if (value instanceof NewUserError) {
                    message = getString(((NewUserError.Error) value.getError()).getValue());
                } else {
                    message = String.format("%s \n --- \n %s", message, value.getExtraInfo());
                }
                ErrorDialog.CreateInstance(EditNewsActivity.this, getString(R.string.error), message, getString(R.string.ok), null, null);
            }
        });

        viewModel.getNavigation().observeEvent(this, navigation -> {
            switch (navigation) {
                case MAIN:
                    SuccessDialog.CreateInstance(this, getString(R.string.success), getString(R.string.news_edited_success), getString(R.string.ok), null, new SuccessDialog.OnSuccessDialogInteraction() {
                        @Override
                        public void onPositiveInteraction() {
                            finish();
                        }

                        @Override
                        public void onNegativeInteraction() {
                            //ignore
                        }
                    });
                    break;
            }
        });
        Timber.d(String.valueOf(newsForEditing.getId()));
        mapView.onCreate(savedInstanceState);
        setField();
        mapChange();
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng latitude_longitude = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            latitude_longitude = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }
        return latitude_longitude;
    }

    public void setField() {
        etNewsTitle.setText(newsForEditing.getTitle());
        etNewsText.setText(newsForEditing.getMessage());
        etNewsAddress.setText(newsForEditing.getAddress());
    }

    @OnClick(R.id.btn_edit_news)
    public void postNewRequest() {

        String title = etNewsTitle.getText().toString();
        String message = etNewsText.getText().toString();
        String latitude = String.valueOf(getLocationFromAddress(this, etNewsAddress.getText().toString()).latitude);
        String longitude = String.valueOf(getLocationFromAddress(this, etNewsAddress.getText().toString()).longitude);
        String address = etNewsAddress.getText().toString();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Title", title)
                .addFormDataPart("message", message)
                .addFormDataPart("location_latitude", latitude)
                .addFormDataPart("location_longitude", longitude)
                .addFormDataPart("Address", address)
                .build();

        viewModel.postEditNews(newsForEditing.getId(), requestBody);

    }

    @OnClick(R.id.ibtn_back)
    public void imageButtonBack() {
        finish();
    }

    @OnClick(R.id.tv_back)
    public void textViewBack() {
        finish();
    }

    public void mapChange() {
        mapView.getMapAsync(googleMap -> {
            mMap = googleMap;
            LatLng location = new LatLng(Double.parseDouble(newsForEditing.getLocation_latitude()), Double.parseDouble(newsForEditing.getLocation_longitude()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            mMap.setOnCameraIdleListener(() -> {
                LatLng centerOfMap = mMap.getCameraPosition().target;
                svNewsItem.setEnableScrolling(true);

                double latitude = centerOfMap.latitude;
                double longitude = centerOfMap.longitude;

                Timber.d("LATLNG:" + centerOfMap.latitude + " " + centerOfMap.longitude);

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                List<Address> addresses = new ArrayList<>();
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String[] street = address.getAddressLine(0).split(",");
                    String streetName = street[0];
                    Timber.d("ADRESS%s", streetName);
                    etNewsAddress.setText(streetName);
                }

            });
            mMap.setOnCameraMoveStartedListener(i -> svNewsItem.setEnableScrolling(false));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

