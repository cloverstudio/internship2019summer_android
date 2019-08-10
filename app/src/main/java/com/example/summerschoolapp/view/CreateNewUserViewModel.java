package com.example.summerschoolapp.view;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.summerschoolapp.common.BaseViewModel;
import com.example.summerschoolapp.model.BigDataResponse;
import com.example.summerschoolapp.model.RequestNewUser;
import com.example.summerschoolapp.model.User;
import com.example.summerschoolapp.repositories.NewUserRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CreateNewUserViewModel extends BaseViewModel {

    private NewUserRepository newUserRepo;

    public CreateNewUserViewModel(@NonNull Application application) {
        super(application);
        newUserRepo = new NewUserRepository();
    }

    public void createNewUser(RequestNewUser newUser) {
        startProgress();
        newUserRepo.postNewUser(newUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<User>() {
                    @Override
                    public void onSuccess(User user) {
                        Timber.d("createdNewUser");
//                        String name = Jwts.parser().setSigningKey(keyUsedWhenSigningJwt).parseClaimsJws("base64EncodedJwtHere").getBody().get("name", String.class);
                        stopProgress();
                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("Failed: %s", e.toString());
                        stopProgress();
                        dispose();
                    }
                });
    }
}
