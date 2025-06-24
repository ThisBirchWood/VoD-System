import { Menu, X } from 'lucide-react';
import MenuButton from "./buttons/MenuButton.tsx";
import clsx from "clsx";
import type {User} from "../utils/types.ts";

type props = {
    sidebarToggled: boolean;
    setSidebarToggled: Function;
    user: User | null;
    className?: string;
}

const Topbar = ({sidebarToggled, setSidebarToggled, user, className}: props) => {
    const apiUrl = import.meta.env.VITE_API_URL;
    const loginUrl = `${apiUrl}/oauth2/authorization/google`;

    return (
        <div className={clsx(className, "flex justify-between")}>
            <MenuButton onClick={() => setSidebarToggled(!sidebarToggled)}>
                {sidebarToggled ? <Menu size={24}/> :  <X size={24}/>}
            </MenuButton>

            <MenuButton className={"w-40 text-gray-600"}
            onClick={() => globalThis.location.href = loginUrl}>
                { user ? user.name : "Login" }
            </MenuButton>
        </div>
    )
}

export default Topbar;