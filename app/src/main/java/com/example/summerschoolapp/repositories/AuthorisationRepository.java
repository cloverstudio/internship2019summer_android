package com.example.summerschoolapp.repositories;

import com.example.summerschoolapp.model.RequestLogin;
import com.example.summerschoolapp.model.RequestRegister;
import com.example.summerschoolapp.model.ResponseLogin;
import com.example.summerschoolapp.model.ResponseSignup;
import com.example.summerschoolapp.network.retrofit.RetrofitAdapter;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class AuthorisationRepository {

    public AuthorisationRepository() {
    }

    public Single<ResponseLogin> postLoginQuery(RequestLogin user) {
        return RetrofitAdapter.getRetrofitClient()
                .login(user)
                .subscribeOn(Schedulers.io());
    }

//    public LiveData<Data> postRegisterQuery(RequestRegister user) {
//        return LiveDataReactiveStreams.fromPublisher(RetrofitAdapter.getRetrofitClient()
//                .register(user)
//                .subscribeOn(Schedulers.io()));
//    }

    public Single<ResponseSignup> postRegisterQuery(RequestRegister user) {
        return RetrofitAdapter.getRetrofitClient()
                .register(user)
                .subscribeOn(Schedulers.io());
    }
}
