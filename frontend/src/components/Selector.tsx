import React from "react";

type props = {
    children: React.ReactNode;
    label: String;
}

const Selector = ({children, label}: props) => {
    return (
        <div className={"flex items-center gap-2"}>
            <label
                className={"w-full"}>
                { label }
            </label>
            <div className="w-px h-6 bg-gray-400 mx-3" />
            {children}
        </div>
    )
}

export default Selector;