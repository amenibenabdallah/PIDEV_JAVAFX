package tn.esprit.services;

import tn.esprit.utils.MyDataBase;
import java.sql.Connection;

public class EvaluationService {
    private final Connection conn;

    public EvaluationService() {
        this.conn = MyDataBase.getInstance().getCnx();
    }
}
