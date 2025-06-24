import { Menu, X } from 'lucide-react';
import MenuButton from "./buttons/MenuButton.tsx";
import clsx from "clsx";

type props = {
    sidebarToggled: boolean;
    setSidebarToggled: Function;
    className?: string;
}

const Topbar = ({sidebarToggled, setSidebarToggled, className}: props) => {
    const apiUrl = import.meta.env.VITE_API_URL;
    const redirectUri = encodeURIComponent(window.location.href);
    const loginUrl = `${apiUrl}/oauth2/authorization/google?redirect_uri=${redirectUri}`;

    return (
        <div className={clsx(className, "flex justify-between")}>
            <MenuButton onClick={() => setSidebarToggled(!sidebarToggled)}>
                {sidebarToggled ? <Menu size={24}/> :  <X size={24}/>}
            </MenuButton>

            <MenuButton className={"w-20 text-gray-600"}
            onClick={() => globalThis.location.href = loginUrl}>
                Login
            </MenuButton>
        </div>
    )
}

export default Topbar;