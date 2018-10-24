package com.aconst.spinareg.controllers;

import com.aconst.spinareg.model.ServiceItemRealm;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class ServiceController {
    private RealmConfiguration realmConfiguration;

    public ServiceController() {
        this.realmConfiguration = Realm.getDefaultConfiguration();
    }

    public Single<List<ServiceItemRealm>> getServices() {
        Realm realm = Realm.getInstance(realmConfiguration);
        List<ServiceItemRealm> itemList =
                realm.copyFromRealm(realm.where(ServiceItemRealm.class).findAll());
        realm.close();
        return Single.just(itemList);
    }

    public Single<ServiceItemRealm> getService(int id) {
        Realm realm = Realm.getInstance(realmConfiguration);
        ServiceItemRealm item = realm.where(ServiceItemRealm.class)
                        .equalTo("servID", id)
                        .findFirst();
        if (item == null) {
            item = new ServiceItemRealm();
            item.setTitle("Услуга id = " + id);
            item.setServID(id);

        } else
            item = realm.copyFromRealm(item);
        realm.close();
        return Single.just(item);
    }

    public Single<Integer> getNextId() {
        Realm realm = Realm.getInstance(realmConfiguration);
        ServiceItemRealm service = realm.where(ServiceItemRealm.class)
                .sort("servID", Sort.DESCENDING).findFirst();
        int result;
        if (service == null) {
            result = 1;
        } else
            result = service.getServID() + 1;
        realm.close();
        return Single.just(result);
    }

    public Completable updateServices(final List<ServiceItemRealm> serviceItemList) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration);
                realm.beginTransaction();
                realm.insertOrUpdate(serviceItemList);
                realm.commitTransaction();
                realm.close();
                Realm.compactRealm(realmConfiguration);
            }
        });
    }

    public Completable deleteServices() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration);
                RealmResults<ServiceItemRealm> services = realm.where(ServiceItemRealm.class)
                        .findAll();
                realm.beginTransaction();
                services.deleteAllFromRealm();
                realm.commitTransaction();
                realm.close();
                Realm.compactRealm(realmConfiguration);
            }
        });
    }

    public Completable deleteService(final int id) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration);
                ServiceItemRealm itemRealm = realm.where(ServiceItemRealm.class)
                        .equalTo("servID", id).findFirst();
                if (itemRealm != null) {
                    realm.beginTransaction();
                    itemRealm.deleteFromRealm();
                    realm.commitTransaction();
                }
                realm.close();
                Realm.compactRealm(realmConfiguration);
            }
        });
    }
}
