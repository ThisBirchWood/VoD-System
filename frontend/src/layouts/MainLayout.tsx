// layout/MainLayout.jsx
import Sidebar from '../components/Sidebar'
import Topbar from '../components/Topbar'
import { Outlet } from 'react-router-dom';
import {useState} from "react";

const MainLayout = () => {
    const [sidebarToggled, setSidebarToggled] = useState(false);

    return (
        <div className={`transition-all duration-300 grid ${sidebarToggled ? "grid-cols-[0px_1fr]" : "grid-cols-[240px_1fr]"} gap-4`}>
            <Sidebar
                className={`row-span-2 transition-all duration-300 ${sidebarToggled ? "-translate-x-full": "translate-x-0"}`}/>
            <Topbar
                className={"transition-all duration-300"}
                sidebarToggled={sidebarToggled}
                setSidebarToggled={setSidebarToggled}/>
            <div className="flex-1 p-4">
                <Outlet/> {/* This renders the nested route content */}
            </div>
        </div>
    )
};

export default MainLayout;