// layout/MainLayout.jsx
import Sidebar from '../components/Sidebar'
import Topbar from '../components/Topbar'
import { Outlet } from 'react-router-dom';
import {useState} from "react";

const MainLayout = () => {
    const [sidebarToggled, setSidebarToggled] = useState(false);

    return (
        <div className="grid grid-cols-[1fr_5fr] min-h-screen">
            <Sidebar className={`row-span-2 transition ${sidebarToggled ? 'hidden' : ''}`}/>
            <Topbar sidebarToggled={sidebarToggled} setSidebarToggled={setSidebarToggled}/>
            <div className="flex-1 p-4">
                <Outlet/> {/* This renders the nested route content */}
            </div>
        </div>
    )
};

export default MainLayout;