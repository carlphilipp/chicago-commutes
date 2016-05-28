package fr.cph.chicago.service;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;

public interface BikeService {

    List<BikeStation> loadAllBikes(@NonNull final Context context);
}
