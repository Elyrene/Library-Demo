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

// GET /api/lends 响应（分页查询借阅记录）
interface LendsRes {
    code: number;
    mes: string;
    count?: number;
    data: lendInfo[];
}

// GET /api/lends/reader/{id} 与 /api/lends/book/{id} 响应（不分页）
interface LendsPlainRes {
    code: number;
    mes: string;
    data: lendInfo[];
}

// GET /api/lends/{id} 响应（单条借阅记录）
interface LendIdRes {
    code: number;
    mes: string;
    data: lendInfo;
}

// POST /api/lends 借书响应
interface LendPostRes {
    code: number;
    mes: string;
}

// PUT /api/lends/{id}/back 还书响应
interface LendBackRes {
    code: number;
    mes: string;
}

// DELETE /api/lends 批量删除响应
interface LendDeleteRes {
    code: number;
    mes: string;
}
