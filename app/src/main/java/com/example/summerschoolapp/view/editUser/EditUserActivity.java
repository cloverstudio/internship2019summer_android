package com.example.summerschoolapp.view.editUser;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.summerschoolapp.R;
import com.example.summerschoolapp.common.BaseActivity;
import com.example.summerschoolapp.common.BaseError;
import com.example.summerschoolapp.dialog.ErrorDialog;
import com.example.summerschoolapp.dialog.SuccessDialog;
import com.example.summerschoolapp.errors.NewUserError;
import com.example.summerschoolapp.model.User;
import com.example.summerschoolapp.utils.Const;
import com.example.summerschoolapp.utils.Tools;
import com.example.summerschoolapp.utils.helpers.EventObserver;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditUserActivity extends BaseActivity {

    public static void StartActivity(Context context, User userForEditing) {

        Intent intent = new Intent(context, EditUserActivity.class);
        intent.putExtra(Const.Intent.USER_DATA, userForEditing);
        context.startActivity(intent);
    }

    @BindView(R.id.ibtn_hide_show)
    ImageButton ibtnHideShow;

    @BindView(R.id.et_edit_user_password)
    EditText etEditUserPassword;

    @BindView(R.id.et_edit_user_name)
    EditText etEditUserName;

    @BindView(R.id.et_edit_user_email)
    EditText etEditUserEmail;

    @BindView(R.id.et_edit_user_oib)
    EditText etEditUserOib;

    @BindView(R.id.iv_user_picture_icon)
    ImageView ivUserPictureIcon;

    @BindView(R.id.civ_edit_user_picture)
    CircleImageView civEditUserPicture;

    @BindView(R.id.tv_edit_user_email)
    TextView tvEditUserEmail;

    @BindView(R.id.tv_edit_user_oib)
    TextView tvEditUserOib;

    @BindView(R.id.tv_edit_user_not_email)
    TextView tvNotEmail;

    @BindView(R.id.tv_edit_user_oib_11)
    TextView tvWrongOib;

    @BindView(R.id.btn_edit_user)
    Button btnEditUser;

    private boolean isVisible = false;
    private EditUserViewModel viewModel;
    private static final int PICK_FROM_GALLERY = 1;
    private File image;
    private String filePath = "";
    private ColorStateList oldColor;

    User userForEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().getExtras() != null) {
            userForEditing = getIntent().getParcelableExtra(Const.Intent.USER_DATA);
        }

        viewModel = ViewModelProviders.of(this).get(EditUserViewModel.class);

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
                ErrorDialog.CreateInstance(EditUserActivity.this, getString(R.string.error), message, getString(R.string.ok), null, null);
            }
        });

        viewModel.getNavigation().observeEvent(this, navigation -> {
            switch (navigation) {
                case MAIN:
                    SuccessDialog.CreateInstance(this, getString(R.string.success), getString(R.string.user_successfully_edited), getString(R.string.ok), null, new SuccessDialog.OnSuccessDialogInteraction() {
                        @Override
                        public void onPositiveInteraction() {
                            finish();
                        }

                        @Override
                        public void onNegativeInteraction() {
                            // ignored
                        }
                    });
                    break;
            }
        });
        setField();
        textChangedListener();
        canUserBeCreated();
        oldColor = tvEditUserOib.getTextColors();
    }

    public void setField() {
        if (userForEditing.getName() == null) {
            etEditUserName.setText(String.format("%s %s", userForEditing.getFirstName(), userForEditing.getLastName()));
        } else {
            etEditUserName.setText(userForEditing.getName());
        }

        if (userForEditing.getPhoto() != null) {
            Glide.with(this)
                    .asBitmap()
                    .fitCenter()
                    .load(Const.Api.API_GET_IMAGE + userForEditing.getPhoto())
                    .into(civEditUserPicture);
            ivUserPictureIcon.setVisibility(View.GONE);
        }
        etEditUserEmail.setText(userForEditing.getEmail());
        etEditUserOib.setText(userForEditing.getOib());
    }

    @OnClick(R.id.ibtn_hide_show)
    public void showHidePassword() {
        if (!isVisible) {
            etEditUserPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ibtnHideShow.setImageDrawable(getResources().getDrawable(R.drawable.log_in_lozinka_icon));
            isVisible = true;
        } else {
            etEditUserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ibtnHideShow.setImageDrawable(getResources().getDrawable(R.drawable.log_in_lozinka_hiden_icon));
            isVisible = false;
        }
    }

    @OnClick(R.id.btn_edit_user)
    public void editUser() {

        String id = userForEditing.getId();
        String oib = etEditUserOib.getText().toString();
        String name = etEditUserName.getText().toString();
        String email = etEditUserEmail.getText().toString();
        String password = Tools.md5(etEditUserPassword.getText().toString());

        if (filePath.equals("") && requiredFieldsFull()) {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("oib", oib)
                    .addFormDataPart("name", name)
                    .addFormDataPart("email", email)
                    .addFormDataPart("password", password)
                    .build();

            viewModel.postEditUser(requestBody, id);
        } else if (!filePath.equals("") && requiredFieldsFull()) {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("oib", oib)
                    .addFormDataPart("name", name)
                    .addFormDataPart("email", email)
                    .addFormDataPart("password", password)
                    .addFormDataPart("photo", "image", uploadPicture(filePath))
                    .build();

            viewModel.postEditUser(requestBody, id);
        } else {
            Toast.makeText(this, getString(R.string.enter_required_fields), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean requiredFieldsFull() {
        return etEditUserName.length() != 0 && etEditUserEmail.length() != 0 && etEditUserOib.length() != 0 && etEditUserPassword.length() != 0;
    }

    @OnClick(R.id.civ_edit_user_picture)
    public void choosePicture() {
        try {
            if (ActivityCompat.checkSelfPermission(EditUserActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditUserActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
            } else {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.ibtn_back)
    public void goBack() {
        finish();
    }

    @OnClick(R.id.tv_back)
    public void tvGoBack() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                }
                break;
        }
    }

    private void textChangedListener() {
        etEditUserOib.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                canUserBeCreated();
                if (etEditUserOib.length() == 0 || etEditUserOib.length() > 11 || etEditUserOib.length() < 11) {
                    tvWrongOib.setTextColor(Color.RED);
                    tvWrongOib.setText(R.string.oib_error_11_characters);
                    tvEditUserOib.setTextColor(Color.RED);
                } else {
                    tvEditUserOib.setTextColor(oldColor);
                    tvWrongOib.setText("");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etEditUserEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                canUserBeCreated();
                if (!isValidEmail(etEditUserEmail.getText().toString().trim())) {
                    tvNotEmail.setTextColor(Color.RED);
                    tvNotEmail.setText(R.string.not_an_email);
                    tvEditUserEmail.setTextColor(Color.RED);
                } else {
                    tvEditUserEmail.setTextColor(oldColor);
                    tvNotEmail.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etEditUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                canUserBeCreated();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etEditUserPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                canUserBeCreated();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private static boolean isValidEmail(CharSequence target) {  // Email validator, checks if field has correct input
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void canUserBeCreated() {
        if (!isValidEmail(etEditUserEmail.getText().toString().trim()) || etEditUserPassword.length() == 0 || etEditUserOib.length() < 11 || etEditUserOib.length() > 11) {
            btnEditUser.setEnabled(false);
            btnEditUser.setAlpha(0.5f);
        } else {
            btnEditUser.setEnabled(true);
            btnEditUser.setAlpha(1.0f);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String mediaPath = cursor.getString(columnIndex);
            cursor.close();

            filePath = mediaPath;

            image = new File(filePath);
            Glide.with(this)
                    .asBitmap()
                    .load(data.getDataString())
                    .into(civEditUserPicture);

            ivUserPictureIcon.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, getString(R.string.failed_to_load), Toast.LENGTH_SHORT).show();
        }
    }

    public RequestBody uploadPicture(String filepath) {
        if (!filepath.equals("")) {
            File file = new File(filepath);

            RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/*"));
            return fileBody;
        } else {
            return null;
        }
    }
}
