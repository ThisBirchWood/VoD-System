// layout/MainLayout.jsx
import Sidebar from '../components/Sidebar'
import Topbar from '../components/Topbar'
import { Outlet } from 'react-router-dom';
import {useEffect, useState} from "react";
import type {User} from "../utils/types";
import { getUser } from "../utils/endpoints";

const MainLayout = () => {
    const [sidebarToggled, setSidebarToggled] = useState(false);
    const [user, setUser] = useState<null | User>(null);

    const fetchUser = async () => {
        try {
            const userData = await getUser();
            setUser(userData);
        } catch (error) {
            console.error('Failed to fetch user:', error);
        }
    };

    useEffect(() => {
        fetchUser();
    }, []);

    return (
        <div className={`transition-all duration-300 grid h-screen ${sidebarToggled ? "grid-cols-[0px_1fr]" : "grid-cols-[240px_1fr]"} grid-rows-[auto_1fr]`}>
            <Sidebar
                user={user}
                className={`row-span-2 transition-all duration-300 overflow-hidden whitespace-nowrap ${sidebarToggled ? "-translate-x-full" : "translate-x-0"}`}/>
            <Topbar
                className={"transition-all duration-300"}
                sidebarToggled={sidebarToggled}
                setSidebarToggled={setSidebarToggled}
                user={user}
                fetchUser={fetchUser}/>
            <div className="overflow-auto">
                <Outlet />
            </div>
        </div>
    );
};
export default MainLayout;