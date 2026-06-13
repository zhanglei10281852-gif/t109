package com.company.material.context;

import lombok.Data;
import java.util.List;

@Data
public class DataPermissionContext {

    private Long userId;
    private String role;
    private String department;
    private String dataScopeType;
    private List<Long> accessibleWarehouseIds;

    private static final ThreadLocal<DataPermissionContext> HOLDER = new ThreadLocal<>();

    public static void set(DataPermissionContext context) {
        HOLDER.set(context);
    }

    public static DataPermissionContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
