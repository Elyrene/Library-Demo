interface readerInfo {
    id: number;
    username: string;
    realName: string;
    sex: string;
    birthday: Date;
    address: string;
    tel: string;
    email: string;
    registerDate: Date;
    readerNumber: string;
    cookie?: string;
}

interface adminInfo {
    id: number;
    username: string;
    adminType: number;
    cookie?: string;
}