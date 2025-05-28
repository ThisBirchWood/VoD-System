// layout/MainLayout.jsx
import Sidebar from '../components/Sidebar'
import Topbar from '../components/Topbar'
import { Outlet } from 'react-router-dom';
import {useState} from "react";

const MainLayout = () => {
    const [sidebarToggled, setSidebarToggled] = useState(false);

    return (
        <div className={`transition-all duration-300 grid h-screen ${sidebarToggled ? "grid-cols-[0px_1fr]" : "grid-cols-[240px_1fr]"} grid-rows-[auto_1fr]`}>
            <Sidebar
                className={`row-span-2 transition-all duration-300 overflow-hidden whitespace-nowrap ${sidebarToggled ? "-translate-x-full" : "translate-x-0"}`}/>
            <Topbar
                className="transition-all duration-300"
                sidebarToggled={sidebarToggled}
                setSidebarToggled={setSidebarToggled}/>
            <div className="overflow-auto">
                <Outlet />
            </div>
        </div>
    );
};
export default MainLayout;