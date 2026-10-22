interface AuthLoginRes {
    code: number;
    mes: string;
}

interface AuthMeRes {
    code: number;
    mes: string;
    data: {
        role: string;
        user: readerInfo | adminInfo;
    }
}

interface AuthLogoutRes {
    code: number;
    mes: string;
}

interface ReadersRegisterRes {
    code: number;
    mes: string;
}

// GET /api/readers/{id} 响应
interface ReadersIdRes {
    code: number;
    mes: string;
    data: readerInfo;
}

// GET /api/admins 响应（分页查询管理员列表）
interface AdminsRes {
    code: number;
    mes: string;
    count?: number;
    data: adminInfo[];
}

// PUT /api/admins/{id}/password 响应
interface AdminsPasswordRes {
    code: number;
    mes: string;
}
