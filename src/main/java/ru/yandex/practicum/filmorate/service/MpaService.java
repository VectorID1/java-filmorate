package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }
    public List<Mpa> findAllMpa() {
        return mpaStorage.findAll();
    }
    public Mpa getMpaById (Long id) {
        return mpaStorage.findById(id).orElseThrow(() -> new NotFoundException("Mpa  с Id " + id + " нет"));
    }


}
